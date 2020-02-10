package it.polito.dp2.BIB.sol3.resources;

import java.math.BigInteger;
import java.net.URI;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.BIB.sol3.service.BookshelvesService;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelves;
import it.polito.dp2.BIB.sol3.service.jaxb.Item;
import it.polito.dp2.BIB.sol3.service.jaxb.Readings;

@Path("/bookshelves")
@Api(value = "/bookshelves")
public class BookshelvesResources {
	public UriInfo uriInfo;
	
	BookshelvesService service;
	
	public BookshelvesResources(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		this.service = new BookshelvesService(uriInfo);
	}

	@GET
	@ApiOperation(value = "getBookshelfs", notes = "search bookshelves"
			)
	@ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK", response=Bookshelves.class),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Bookshelves getBookshelfs(
			@ApiParam("The keyword to be used as prefix for the search") @QueryParam("keyword") @DefaultValue("") String keyword
			) {
		if (keyword==null)
			throw new BadRequestException("keyword is required");
		try {
			return service.getBookshelves(keyword, BigInteger.valueOf(1));
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}
	
	@POST
	@ApiOperation(value = "createBookshelf", notes = "create a new bookshelf", response=Bookshelf.class
	)
	@ApiResponses(value = {
		    @ApiResponse(code = 201, message = "Created", response=Bookshelf.class),
		    @ApiResponse(code = 400, message = "Bad Request"),
		    })
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response createBookshelf(@ApiParam("The keyword to be used as prefix for the search") @QueryParam("name") String bname) {
		try {
			Bookshelf returnBookshelf = service.createBookshelf(bname);
			return Response.created(new URI(returnBookshelf.getSelf())).entity(returnBookshelf).build();
		} catch (Exception e1) {
			throw new InternalServerErrorException();
		}
	}
	
	@GET
	@Path("/{bsid}")
    @ApiOperation(value = "getBookshelf", notes = "read a single bookshelf"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK", response=Bookshelf.class),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Bookshelf getBookeshelf(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid) {
		Bookshelf bookshelf;
		try {
			bookshelf = service.getBookshelf(bsid);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		if (bookshelf == null)
			throw new NotFoundException();
		return bookshelf;
	}
	
	@DELETE
	@Path("/{bsid}")
    @ApiOperation(value = "deleteBookshelf", notes = "delete a single bookshelf"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	public void deleteBookshelf(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid) {
		BigInteger ret;
		try {
			ret = service.deleteBookshelf(bsid);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		if (ret==null)
			throw new NotFoundException();
		return;
	}
	
	@GET
	@Path("/{bsid}/{cid}")
    @ApiOperation(value = "getItemOfBookshelf", notes = "read an item from bookshelf"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK", response=Item.class),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Item getItemOfBookshelf(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid,
			@ApiParam("The id of the item contained into the bookshelf") @PathParam("cid") BigInteger cid) throws Exception {

		Item item ;
		try {
			
			item = service.getItemOfBookshelf(bsid, cid);
			
			if (item == null){
				throw new NotFoundException();
			}
			
			return item;
			
		} catch (NotFoundException e) {			
			throw new ClientErrorException(Response.Status.NOT_FOUND);
			
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}		
	}
	
	@POST
	@Path("/{bsid}/{cid}")
    @ApiOperation(value = "addItem", notes = "update a single item"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK", response=Item.class),
    		@ApiResponse(code = 400, message = "Bad Request"), 
    		@ApiResponse(code = 403, message = "Forbidden"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Item addItem(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid,
			@ApiParam("The id of the item to add to the bookshelf") @PathParam("cid") BigInteger cid) throws Exception {

		Item item;
		try {
			item = service.addItemToBookshelf(bsid, cid);
		} catch (ForbiddenException e) {			
			throw new ClientErrorException(Response.Status.FORBIDDEN);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		if (item == null)
			throw new NotFoundException();
		return item;
	}
	
	@DELETE
	@Path("/{bsid}/{cid}")
    @ApiOperation(value = "deleteItemFromBookshelf", notes = "delete a single item from a bookshelf"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	public void deleteItemFromBookshelf(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid,
			@ApiParam("The id of the item to remove from the bookshelf") @PathParam("cid") BigInteger cid) throws Exception {
		String ret;

		try {
			ret = service.deleteItemFromBookshelf(bsid, cid);
			
			if (ret == null)
				throw new NotFoundException();
			
		} catch (NotFoundException e) {
			throw new ClientErrorException(Response.Status.NOT_FOUND);
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new InternalServerErrorException();
		}
			
		return;
	}
	
	@GET
	@Path("/{bsid}/readings")
    @ApiOperation(value = "getReadings", notes = "read the number of reads from a bookshelf"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK", response=Readings.class),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Readings getReadings(
			@ApiParam("The id of the bookshelf") @PathParam("bsid") BigInteger bsid) throws Exception {
		Readings read;
		try {
			read = service.getReadings(bsid);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		if (read == null)
			throw new NotFoundException();
		return read;
	}
}
