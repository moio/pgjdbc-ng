/**
 * Copyright (c) 2013, impossibl.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of impossibl.com nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.impossibl.postgres.datetime;

import static com.impossibl.postgres.datetime.FormatUtils.checkOffset;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

import java.util.Calendar;
import java.util.Map;

import com.impossibl.postgres.datetime.instants.FutureInfiniteInstant;
import com.impossibl.postgres.datetime.instants.Instant;
import com.impossibl.postgres.datetime.instants.PastInfiniteInstant;

public class ISOTimestampFormat implements DateTimeFormat {
	
	Parser parser = new Parser();
	Printer printer = new Printer();

	@Override
	public Parser getParser() {
		return parser;
	}

	@Override
	public Printer getPrinter() {
		return printer;
	}
	
		
	static class Parser implements DateTimeFormat.Parser {
		
		ISODateFormat.Parser dateParser = new ISODateFormat.Parser();
		ISOTimeFormat.Parser timeParser = new ISOTimeFormat.Parser();
		
		@Override
		public int parse(String date, int offset, Map<String, Object> pieces) {
	
			try {
				
				if(date.equals("infinity")) {
					pieces.put(INFINITY_PIECE, FutureInfiniteInstant.INSTANCE);
					offset = date.length();
				}
				else if(date.equals("-infinity")) {
					pieces.put(INFINITY_PIECE, PastInfiniteInstant.INSTANCE);
					offset = date.length();
				}
				else {

					offset = dateParser.parse(date, offset, pieces);
					checkOffset(date, offset, '\0');
					
					if(offset < date.length()) {
						
						char sep = date.charAt(offset);
						if(sep != ' ' && sep != 'T') {
							return ~offset;
						}
						
						offset = timeParser.parse(date, offset + 1, pieces);
						if(offset < 0) {
							return offset;
						}
						
					}
					
				}
				
			}
			catch(IndexOutOfBoundsException | IllegalArgumentException e) {
			}
			
			return offset;
		}
		
	}

	
	static class Printer implements DateTimeFormat.Printer {
		
	  @Override
		public String format(Instant instant) {
	  	
	  	Calendar cal = Calendar.getInstance(instant.getZone());
	  	cal.setTimeInMillis(MICROSECONDS.toMillis(instant.getMicrosUTC()));
	
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH) + 1;
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	
	    char buf[] = "2000-00-00".toCharArray();
	    buf[0] = Character.forDigit(year/1000,10);
	    buf[1] = Character.forDigit((year/100)%10,10);
	    buf[2] = Character.forDigit((year/10)%10,10);
	    buf[3] = Character.forDigit(year%10,10);
	    buf[5] = Character.forDigit(month/10,10);
	    buf[6] = Character.forDigit(month%10,10);
	    buf[8] = Character.forDigit(day/10,10);
	    buf[9] = Character.forDigit(day%10,10);
	
	    return new String(buf);
		}
	  
	}

}