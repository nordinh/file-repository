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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.codahale.metrics.annotation.Timed;
import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Path("/{bucket}")
public class FileResource {

	private DB db;

	public FileResource(DB db) {
		this.db = db;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Timed
	public Response storeFile(
			@PathParam("bucket") String bucket,
			@FormDataParam("file") InputStream fileInputStream,
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
	public Response readFile(
			@PathParam("bucket") String bucket,
			@PathParam("fileName") String fileName) {

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
	public void deleteFile(
			@PathParam("bucket") String bucket,
			@PathParam("fileName") String fileName) {

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
