package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }
    
    private String[] SplitUserData(String data) {
    	try {
    	String UserDataUnSplitted[] = new String[4];
    	String Users[] = new String[4];
    	
    	UserDataUnSplitted = data.split("& ");
    	
    	Users[0] = UserDataUnSplitted[0].split("=")[1];
    	Users[1] = UserDataUnSplitted[1].split("=")[1];
    	Users[2] = UserDataUnSplitted[2].split("=")[1];
    	
    	return Users;
    	
    	} catch(Exception e) {
    		log.error(String.format("%s : %s", "Split User Information Error", data));
    	}
		return null;
    }
    
    private void GetMethodHandler(DataOutputStream dos, String GetData) throws IOException {
    	//GetData가 특정 html을 포함하고 있다면 그냥 GetData처리하면 됨
    	//그렇지 않고, /user/create를 내포하고 있으면 Parsing해서 던지는 걸로.
    	byte[] body = null;
    	if(GetData.contains(".html"))
    		body = Files.readAllBytes(new File("./webapp" + GetData).toPath());
    	else if(GetData.contains("/user/create?"))
    	{
    		String splits[] = new String[2];
    		splits = GetData.split("?");
    		String UserData[] = SplitUserData(splits[1]);
    		
    		//model.User usr = new model.User();
    		
    	}
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    
    private void PostMethodHandler(DataOutputStream dos, String PostData) throws IOException {
    	byte[] body = Files.readAllBytes(new File("./webapp" + PostData).toPath());
    	response302Header(dos, "test");
    	responseBody(dos, body);
    }
    
    private void DefautlHandler(DataOutputStream dos) throws IOException {
    	byte[] body = "Hello World".getBytes();
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        	String line = null;

        	line = bf.readLine();
        	if(line == null) return;

        	String[] tokens = line.split(" ");
        	
        	log.debug(line);
        	
        	while(!line.equals("")) 
        	{
        		line = bf.readLine();
            	log.debug(line);
        	}
        	
            DataOutputStream dos = new DataOutputStream(out);
        	
        	if(tokens[0].toUpperCase().equals("GET"))
        	{
        		GetMethodHandler(dos, tokens[1]);
        		return;
        	}
        	else if(tokens[0].toUpperCase().equals("POST"))
        	{
        		PostMethodHandler(dos, tokens[1]);
        		return;
        	}
        	
        	DefautlHandler(dos);
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes(String.format("Location: %s\r\n", location));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
