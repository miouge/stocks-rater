package com.github.stockRater.beans;

public class AnswerHandler {
	
	protected String answer;
	
	
	public void assertPatternNoPresent( String pattern, String message ) throws Exception {
		
		if( answer.contains( pattern ) == true ) {
			throw new Exception( message );
		}
	}
	
	protected void checkAnswer() throws Exception {
		// to override
	}
	
	public void processAnswerHTML( Stock stock, StringBuilder answer ) throws Exception {
		
		// answer is a HTML document
		
		System.out.println( String.format( "handler for %s", stock.getName()));
		
		

	}
	
	
	public void processAnswer( StringBuilder answer ) throws Exception {
		
		// answer is a json string form

		if( answer != null ) {

/*			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
			mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

			ResponseBody responseBody = mapper.readValue( answer, ResponseBody.class );

			int responseCode = responseBody.getMessage().getCode();
			
			String feedback = String.format( "\t=> code <%d> answer size =%d message <%s>\n",
					responseBody.getMessage().getCode(), answer.length(), responseBody.getMessage().getText() );
			
			if( responseCode == 0 ) {
				
				System.out.print( feedback );

				this.answer = answer;
				this.checkAnswer();
				
			} else {

				System.err.print( feedback );
				throw new Exception( "StatusMessage code not = 0");
			}
*/
		}
	}
}
