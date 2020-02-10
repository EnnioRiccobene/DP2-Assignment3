package it.polito.dp2.BIB.sol3.service;

import java.math.BigInteger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.UriInfo;

import it.polito.dp2.BIB.sol3.db.BookshelvesDB;
import it.polito.dp2.BIB.sol3.db.DB;
import it.polito.dp2.BIB.sol3.db.Neo4jDB;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelves;
import it.polito.dp2.BIB.sol3.service.jaxb.Item;
import it.polito.dp2.BIB.sol3.service.jaxb.Readings;
import it.polito.dp2.BIB.sol3.service.util.ResourseUtils;

public class BookshelvesService {
	private DB n4jDb = Neo4jDB.getNeo4jDB();
	private BookshelvesDB db;
	ResourseUtils rutil; 
	
	public BookshelvesService(UriInfo uriInfo){
		rutil = new ResourseUtils((uriInfo.getBaseUriBuilder()));
		db = BookshelvesDB.getInstance();
	}
	
	public Bookshelves getBookshelves(String keyword, BigInteger page) throws Exception {

		Bookshelves bookshelves = new Bookshelves();
		bookshelves.setPage(page);
		bookshelves.setTotalPages(BigInteger.valueOf(1));
		bookshelves.setNext(null);
		for(Bookshelf b : db.getBookshelves().values()){
			if(b.getBookshelfName().startsWith(keyword))
				bookshelves.getBookshelf().add(b);
		}		
		
		return bookshelves;		
	}
	
	public Bookshelf createBookshelf(String bname) throws Exception {
		
		for(Bookshelf b : db.getBookshelves().values()){
			if(b.getBookshelfName().equals(bname))
				return b;
		}
		
		//setto id univoco per il bookshelf	
		int id = db.getIdCount().incrementAndGet();
		
		Readings read = new Readings();
		read.setNumberOfReads(BigInteger.valueOf(0));
		Bookshelf bookshelf = new Bookshelf();
		bookshelf.setBookshelfName(bname);
		rutil.completeBookshelf(bookshelf, BigInteger.valueOf(id));
		rutil.completeReading(bookshelf, BigInteger.valueOf(id), read);
		db.getBookshelves().put(BigInteger.valueOf(id), bookshelf);
		db.getReadsMap().put(BigInteger.valueOf(id), read);

		return bookshelf;
	}

	public Bookshelf getBookshelf(BigInteger id) {
		Bookshelf bookshelf = db.getBookshelves().get(id);
		if (bookshelf != null) {
			rutil.completeBookshelf(bookshelf, id); //forse questa non serve
			int readCount = db.getReadsMap().get(id).getNumberOfReads().intValue() + 1;
			synchronized(this) {
				db.getReadsMap().get(id).setNumberOfReads(BigInteger.valueOf(readCount));
			}
						
		}
		return bookshelf;
	}

	public synchronized BigInteger deleteBookshelf(BigInteger id) {
		if(db.getBookshelves().containsKey(id)) {
			db.getBookshelves().remove(id);
			return id;
		}
		else
			return null;
	}

	public Item getItemOfBookshelf(BigInteger id, BigInteger cid) {
		Bookshelf bookshelf = db.getBookshelves().get(id);
		Readings reads = db.getReadsMap().get(id);
		if(bookshelf == null) {
			return null;
		}

		for(Item it : bookshelf.getItem()){
			
			String itSelf = "";
			if(it.getSelf().startsWith("http://localhost:8080/BiblioSystem/rest/")){
				itSelf = it.getSelf().replace("http://localhost:8080/BiblioSystem/rest/biblio/items/", "");
			} else {
				if(!it.getSelf().startsWith("http://localhost/BiblioSystem/rest/")){
					//codice per ottenere il giusto url da togliere alla stringa
					String portToClean = it.getSelf().replace("http://localhost:", "");
					String port = "";     //substring containing first 4 characters
					 
					if (portToClean.length() > 4) {
					    port = portToClean.substring(0, 4);
					} else {
					    port = portToClean;
					}
					String toReplace = "http://localhost:" + port +"/BiblioSystem/rest/biblio/items/";
					itSelf = it.getSelf().replace(toReplace, "");
				} else {
					itSelf = it.getSelf().replace("http://localhost/BiblioSystem/rest/biblio/items/", "");
				}
			}
			
			if(itSelf.equals(String.valueOf(cid.intValue())) ) {

				int readCount = reads.getNumberOfReads().intValue() + 1;
				synchronized(this){
					db.getReadsMap().get(id).setNumberOfReads(BigInteger.valueOf(readCount));
				}
				return it;
			}
		}

		return null;
	}

	public Item addItemToBookshelf(BigInteger bsid, BigInteger cid) throws Exception {
		Item item = n4jDb.getItem(cid);
		if (item != null) {
			rutil.completeItem(item, cid);
			if(db.getBookshelves().get(bsid) == null)
				return null;
			if(db.getBookshelves().get(bsid).getItem().size() >= 20)
				throw new ForbiddenException("Limit of items into the bookshelf reached");
			else
				db.getBookshelves().get(bsid).getItem().add(item);
		}
		return item;
	}

	public String deleteItemFromBookshelf(BigInteger bsid, BigInteger cid) {
		if(db.getBookshelves().containsKey(bsid)) {
			for(int i = 0; i < db.getBookshelves().get(bsid).getItem().size(); i++){
				Item itemTmp = new Item();
				rutil.completeItem(itemTmp, cid);
				if(itemTmp.getSelf().equals(db.getBookshelves().get(bsid).getItem().get(i).getSelf())){
					String deletedSelf = db.getBookshelves().get(bsid).getItem().get(i).getSelf();
					
					db.getBookshelves().get(bsid).getItem().remove(i); 							

					return deletedSelf;
					
				}
			}
			return null; //item not exist
		}
		else
			return null; //bookshef not exist
	}

	public Readings getReadings(BigInteger bsid) {
		if(db.getReadsMap().containsKey(bsid)){
			return db.getReadsMap().get(bsid);
		} else return null;
	}
}
