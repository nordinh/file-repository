file-repository 
===============

File repository implementation powered by MongoDB GridFS and accessible through simple rest interface.

Starting application
--------------------
Running `mvn clean install` on the project will result in a fat jar being created. Running `java -jar target/file-repository-0.0.1-SNAPSHOT server config.yml` will boot the application. As an alternative you can also run the `com.github.nordinh.filerepository.FileRepositoryApplication` main method. The application will by default run on port 8181, admin access (metrics, ...) on port 8182. You will need a running MongoDB instance on your local machine accessible on port 27017. See `config.yml` for details.

Buckets
-------
The file repository is organized in buckets. Each bucket has a name and can contain an unlimited number of files of unlimited size. E.g. `http://<host>/reports` or `http://<host>/attachments` or ... These buckets offer an easy way to organize the different types of files stored in the repository. Buckets are being created by the service on the fly, i.e. the first time the bucket is used. It shouldn't be that difficult to limit access to certain buckets through URL security policies.

Uploading files
---------------
Uploading a file can be done using a HTTP POST request with content type multipart/form-data. The name of the file parameter should be 'file'. If successfull the response will have status code 201 and the location header will contain the URL of the file. If a file already exists in that bucket with exactly the same name, it will get overwritten.

###Request:

    POST /mybucket HTTP/1.1
    Host: localhost:8181
    Connection: keep-alive
    Content-Length: 100505
    Content-Type: multipart/form-data; boundary=mymultipartboundary
    
    --mymultipartboundary
	Content-Disposition: form-data; name="file"; filename="myfile.png"
	Content-Type: image/png
	
	<file data>
	--mymultipartboundary--

###Response:
	HTTP/1.1 201 Created
	Date: Wed, 28 Jan 2015 07:02:02 GMT
	Location: http://localhost:8181/mybucket/myfile.png
	Content-Length: 0


Retrieving files
----------------
Retrieving a file can be done using a HTTP GET request.

###Request

	GET /mybucket/myfile.png HTTP/1.1
	Host: localhost:8181

###Response

	HTTP/1.1 200 OK
	Date: Wed, 28 Jan 2015 07:12:08 GMT
	Content-Type: application/octet-stream
	Transfer-Encoding: chunked
	
	<file data>

Deleting files
--------------
Deleting a file can be done using a HTTP DELETE request.

###Request

	DELETE /mybucket/myfile.png HTTP/1.1
	Host: localhost:8181

###Response

	HTTP/1.1 204 No Content
	Date: Wed, 28 Jan 2015 07:17:40 GMT