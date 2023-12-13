package gov.cdc.dhcs.hehr.dcp.direct.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.cdc.dhcs.hehr.dcp.direct.service.DocumentRepository;

/**
 * 
 * @author Sai Valluripalli
 *
 */
@RestController
@RequestMapping("/api")
public class AppController {
	
	private static final Logger logger = LoggerFactory.getLogger(AppController.class);
	
	@Autowired
	private DocumentRepository documentRepository;
	
	@DeleteMapping("/clean/all")
	public ResponseEntity<Map<String, String>> cleanUpAll(){
		logger.info("Clean all document request received");
		this.documentRepository.deleteAll();
		Map<String, String> response = new HashMap<String,String>();
		response.put("message", "Successfully cleaned up the in-memory data store");
		return new ResponseEntity<Map<String,String>>(response, HttpStatus.OK);
	}
	
	@DeleteMapping("/clean/{name}")
	public ResponseEntity<Map<String, String>> deleteItem(@PathVariable("name") String name){
		Assert.notNull(name, "Name cannot be null. Please provide name at the request URL");
		this.documentRepository.deleteItem(name);
		Map<String, String> response = new HashMap<String,String>();
		response.put("message", "Successfully deleted " + name + " from the in-memory data store");
		return new ResponseEntity<Map<String,String>>(response, HttpStatus.OK);
	}

}
