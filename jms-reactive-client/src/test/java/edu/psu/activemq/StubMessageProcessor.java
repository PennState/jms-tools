package edu.psu.activemq;

import javax.jms.Message;

import edu.psu.activemq.exception.UnableToProcessMessageException;

public class StubMessageProcessor extends MessageProcessor {

	@Override
	protected void handleMessage(Message message) throws UnableToProcessMessageException {
		
	}
}