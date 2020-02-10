package it.polito.dp2.BIB.sol3.db;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;
import it.polito.dp2.BIB.sol3.service.jaxb.Readings;

public class BookshelvesDB {
	private static BookshelvesDB istance=null;

	
	private ConcurrentHashMap<BigInteger, Bookshelf> bookshelvesMap;
	//contatore dei bookshelves, non viene decrementato quando ne elimino uno e serve per assegnare un id univoco ai bookshelf
	private AtomicInteger idCount; 
	private ConcurrentHashMap<BigInteger, Readings> readsMap;
	
	private BookshelvesDB() {
		bookshelvesMap = new ConcurrentHashMap<>();
		idCount = new AtomicInteger(0);
		readsMap = new ConcurrentHashMap<>();
	}
	
	public static BookshelvesDB getInstance(){
		if(istance==null)
			istance = new BookshelvesDB();
		return istance;
	}

	public ConcurrentHashMap<BigInteger, Bookshelf> getBookshelves() {
		return bookshelvesMap;
	}

	public ConcurrentHashMap<BigInteger, Readings> getReadsMap() {
		return readsMap;
	}

	public AtomicInteger getIdCount() {
		return idCount;
	}

	public void setIdCount(AtomicInteger idCount) {
		this.idCount = idCount;
	}
	
}
