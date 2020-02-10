package it.polito.dp2.BIB.sol3.client;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.BIB.ass3.Bookshelf;
import it.polito.dp2.BIB.ass3.DestroyedBookshelfException;
import it.polito.dp2.BIB.ass3.ItemReader;
import it.polito.dp2.BIB.ass3.ServiceException;
import it.polito.dp2.BIB.ass3.TooManyItemsException;
import it.polito.dp2.BIB.ass3.UnknownItemException;
import it.polito.dp2.BIB.sol3.client.Bookshelf.Item;
import it.polito.dp2.BIB.sol3.client.Readings;

public class MyBookshelf implements Bookshelf {
	
	private WebTarget btarget;
	private WebTarget target;
	
	private it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf b;
	//serve per catturare DestroyedBookshelfException localmente dopo che lo ottengo la prima volta
	private boolean destroyedBookshelf; 

	public MyBookshelf(it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf b, WebTarget btarget, WebTarget target) {
		this.btarget = btarget;
		this.target = target;
		this.destroyedBookshelf = false;
		
		this.b = b;
	}
	
	//usato per ottenere l'id del bookshelf corrente da usare negli altri metodi
	private String getBookshelfId(){
		String selfB = this.b.getSelf();
		String ret = "";
		if(selfB.startsWith("http://localhost:8080/BiblioSystem/rest/bookshelves")) {
			ret = selfB.replace("http://localhost:8080/BiblioSystem/rest/bookshelves/", "");
		} else {
			if(!selfB.startsWith("http://localhost/BiblioSystem/rest/")){
				//codice per ottenere il giusto url da togliere alla stringa
				String portToClean = selfB.replace("http://localhost:", "");
				String port = "";     //substring containing first 4 characters
				 
				if (portToClean.length() > 4) {
				    port = portToClean.substring(0, 4);
				} else {
				    port = portToClean;
				}
				String toReplace = "http://localhost:" + port +"/BiblioSystem/rest/bookshelves/";
				ret = selfB.replace(toReplace, "");
			} else {
				ret = selfB.replace("http://localhost/BiblioSystem/rest/bookshelves/", "");
			}
		}
		return ret;
	}

	@Override
	public String getName() throws DestroyedBookshelfException {

		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}

		Response response = btarget.path(this.getBookshelfId())
				.request()
				.accept(MediaType.APPLICATION_JSON).get();
		
		if (response.getStatus() == 404) {
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		
		return this.b.getBookshelfName();
	}

	@Override
	public void addItem(ItemReader item)
			throws DestroyedBookshelfException, UnknownItemException, TooManyItemsException, ServiceException {
		
		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}		
		
		if(!(item instanceof ItemReaderImpl)){
			throw new UnknownItemException();
		}
		
		Items items = target.path("items")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(Items.class);
		
		String cid = null;
		boolean exist = false;
		for(it.polito.dp2.BIB.sol3.client.Items.Item i : items.item){
			
			if(i.title.equals(item.getTitle()) && (item.getAuthors().length == i.getAuthor().size())){
				
				boolean sameAuthors = true;
				for(int j = 0; j < i.getAuthor().size(); j++){
					if(!(i.getAuthor().get(j).equals(item.getAuthors()[j]))) {
						sameAuthors = false;
						break;
					}
				}
				if(sameAuthors){
					if(i.getSelf().startsWith("http://localhost:8080/BiblioSystem/rest")){
						cid = i.getSelf().replace("http://localhost:8080/BiblioSystem/rest/biblio/items/", "");
					} else {
						if(!i.getSelf().startsWith("http://localhost/BiblioSystem/rest/")){
							//codice per ottenere il giusto url da togliere alla stringa
							String portToClean = i.getSelf().replace("http://localhost:", "");
							String port = "";     //substring containing first 4 characters
							 
							if (portToClean.length() > 4) {
							    port = portToClean.substring(0, 4);
							} else {
							    port = portToClean;
							}
							String toReplace = "http://localhost:" + port +"/BiblioSystem/rest/biblio/items/";
							cid = i.getSelf().replace(toReplace, "");
						} else {
							cid = i.getSelf().replace("http://localhost/BiblioSystem/rest/biblio/items/", "");
						}
					}
					
					exist = true;
					break;
				}
			}
		}
		
		if(exist == false) {
			throw new UnknownItemException();
		}
		
		Response response = btarget.path(this.getBookshelfId()).path(cid)
				.request()
				.accept(MediaType.APPLICATION_JSON).post(Entity.json(null)); 
		
		if (response.getStatus() == 404) {
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		if (response.getStatus() == 403) {
			throw new TooManyItemsException();
		}
		if (response.getStatus() != 200) {		
			throw new ServiceException();
		}

	}

	@Override
	public void removeItem(ItemReader item) throws DestroyedBookshelfException, UnknownItemException, ServiceException {
		
		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}	
		
		if(!(item instanceof ItemReaderImpl)){
			throw new UnknownItemException();
		}

		Response response = btarget.path(this.getBookshelfId())
				.request()
				.accept(MediaType.APPLICATION_JSON).get();
		
		if (response.getStatus() == 404){
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		
		it.polito.dp2.BIB.sol3.client.Bookshelf bookshelf = response.readEntity(it.polito.dp2.BIB.sol3.client.Bookshelf.class);
		
		String cid = null;
		boolean exist = false;
		for(Item i : bookshelf.getItem()){
			if(i.getTitle().equals(item.getTitle()) && (item.getAuthors().length == i.getAuthor().size())) {
				boolean sameAuthors = true;
				for(int j = 0; j < i.getAuthor().size(); j++){
					if(!(i.getAuthor().get(j).equals(item.getAuthors()[j]))) {
						sameAuthors = false;
						break;
					}
				}
				if(sameAuthors){
					if(i.getSelf().startsWith("http://localhost:8080/BiblioSystem/rest")){
						cid = i.getSelf().replace("http://localhost:8080/BiblioSystem/rest/biblio/items/", "");
					} else {
						if(!i.getSelf().startsWith("http://localhost/BiblioSystem/rest/")){
							//codice per ottenere il giusto url da togliere alla stringa
							String portToClean = i.getSelf().replace("http://localhost:", "");
							String port = "";     //substring containing first 4 characters
							 
							if (portToClean.length() > 4) {
							    port = portToClean.substring(0, 4);
							} else {
							    port = portToClean;
							}
							String toReplace = "http://localhost:" + port +"/BiblioSystem/rest/biblio/items/";
							cid = i.getSelf().replace(toReplace, "");
						} else {
							cid = i.getSelf().replace("http://localhost/BiblioSystem/rest/biblio/items/", "");
						}
					}
					
					exist = true;
					break;
				}
			}
		}
		
		if(exist == false) {			
			return;
		}
		
		Response response2 = btarget.path(this.getBookshelfId()).path(cid)
				.request()
				.accept(MediaType.APPLICATION_JSON).delete();
		
		if (response2.getStatus() == 404) {
			//devo capire se non ho trovato il bookshelf o l'item al suo interno perché entrambi mi causano 404
			Response response3 = btarget.path(this.getBookshelfId())
					.request()
					.accept(MediaType.APPLICATION_JSON).get();
			
			if (response3.getStatus() == 404){
				this.destroyedBookshelf = true;
				throw new DestroyedBookshelfException();
			} else {
				throw new UnknownItemException();
			}
		}
		if(response2.getStatus() != 204) {
			throw new ServiceException();
		}
	}

	@Override
	public Set<ItemReader> getItems() throws DestroyedBookshelfException, ServiceException {

		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}		

		Set<ItemReader> set = new HashSet<>();
		
		Response response = btarget.path(this.getBookshelfId())
				.request()
				.accept(MediaType.APPLICATION_JSON).get();
		
		if (response.getStatus() == 404) {
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		if (response.getStatus() != 200) {
			throw new ServiceException();
		}
		
		response.bufferEntity();
		it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf booksh = response.readEntity(it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf.class);
		
		for(it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf.Item ite : booksh.getItem()){
			set.add(new ItemReaderImpl(ite));
		}

		return set;
	}

	@Override
	public void destroyBookshelf() throws DestroyedBookshelfException, ServiceException {

		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}		
		
		Response response = btarget.path(this.getBookshelfId())
				.request()
				.accept(MediaType.APPLICATION_JSON).delete();
		
		if (response.getStatus() == 404) {
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		if (response.getStatus() != 204) {
			throw new ServiceException();
		}
	}

	@Override
	public int getNumberOfReads() throws DestroyedBookshelfException, ServiceException {
		
		if(this.destroyedBookshelf == true){
			throw new DestroyedBookshelfException();
		}		
		
		Response response = btarget.path(this.getBookshelfId()).path("readings")
				.request()
				.accept(MediaType.APPLICATION_JSON).get();
		
		if (response.getStatus() == 404) {
			this.destroyedBookshelf = true;
			throw new DestroyedBookshelfException();
		}
		if (response.getStatus() != 200) {
			throw new ServiceException();
		}
		
		response.bufferEntity();
		Readings read = response.readEntity(Readings.class);
		
		return read.getNumberOfReads().intValue();
	}

}
