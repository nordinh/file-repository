package com.github.nordinh.filerepository.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import com.codahale.metrics.annotation.Timed;
import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/{bucket}")
@Api(
		value = "File Store",
		description = "File operations for a particular bucket")
public class FileResource {

	private DB db;

	public FileResource(DB db) {
		this.db = db;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Timed
	@ApiOperation(
			value = "Store a file in the repository",
			notes = "Stores a file sent using multipart form data into the bucket. "
					+ "Files are identified by their file name. "
					+ "If a file with the same name already exists it will get replaced.")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "The file has been stored and is accessible on the given location")
	})
	public Response storeFile(
			@PathParam("bucket") @ApiParam(value = "The bucket in which to store the file", required = true) String bucket,
			@FormDataParam("file") @ApiParam(value = "The (binary) file to be stored", required = true) InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {

		GridFS gridFS = new GridFS(db, bucket);
		gridFS.remove(contentDispositionHeader.getFileName());
		GridFSInputFile createdFile = gridFS.createFile(fileInputStream, contentDispositionHeader.getFileName());
		createdFile.save();

		return Response
				.created(
						UriBuilder
								.fromResource(FileResource.class)
								.build(encode(contentDispositionHeader.getFileName())))
				.build();
	}

	@GET
	@Path("{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Timed
	@ApiOperation(
			value = "Read a file from the repository",
			notes = "Reads the file identified by the given file name from the bucket. Returns a binary stream.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "File was found"),
			@ApiResponse(code = 500, message = "File was not found")
	})
	public Response readFile(
			@PathParam("bucket") @ApiParam(value = "The bucket from which to read the file", required = true) String bucket,
			@PathParam("fileName") @ApiParam(value = "The file name of the file to be retrieved", required = true) String fileName) {

		GridFS gridFS = new GridFS(db, bucket);
		GridFSDBFile gridFSDBFile = gridFS.findOne(decode(fileName));

		return Response
				.ok(
						new StreamingOutput() {
							@Override
							public void write(OutputStream output) throws IOException, WebApplicationException {
								try {
									ByteStreams.copy(gridFSDBFile.getInputStream(), output);
								} catch (Exception e) {
									throw new WebApplicationException(e);
								}
							}
						})
				.build();
	}

	@DELETE
	@Path("{fileName}")
	@Timed
	@ApiOperation(
			value = "Delete a file from the repository",
			notes = "Deletes the file identified by the given file name from the bucket. If the file does not exist nothing happens.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "File deletion was successfull")
	})
	public void deleteFile(
			@PathParam("bucket") @ApiParam(value = "The bucket from which to delete the file", required = true) String bucket,
			@PathParam("fileName") @ApiParam(value = "The file name of the file to be deleted", required = true) String fileName) {

		GridFS gridFS = new GridFS(db, bucket);
		gridFS.remove(decode(fileName));
	}

	private String encode(String fileName) {
		try {
			return URLEncoder.encode(fileName, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Problems with file name");
		}
	}

	private String decode(String fileName) {
		try {
			return URLDecoder.decode(fileName, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Problems with file name");
		}
	}
}
