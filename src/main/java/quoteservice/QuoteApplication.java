package quoteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;

import java.sql.*;


@SpringBootApplication
@RestController

public class QuoteApplication {
	public static Connection connection = null;

	public static void main(String[] args) {

		try{
			connection = DriverManager.getConnection( "jdbc:h2:mem:testdb;INIT=RUNSCRIPT FROM 'classpath:init.sql'",
					"sa", "");

		}catch(SQLException e){
			e.printStackTrace();
			// exit app or something??
		}


		SpringApplication.run(QuoteApplication.class, args);
	}

	@GetMapping
	public String homepage() {
		return "hello world";
	}

	/*
	close db connection when exiting ?
	can it deal with bb.to
	 */
	@RequestMapping(path = "symbols/{ticker}/quotes/latest")
	public ResponseEntity<String> latestQuote(@PathVariable("ticker") String ticker){
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		double bid =0;
		double ask =0;

		if (ticker.length() < 2 || ticker.length() > 6){ //  invalid symbol
			return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		try {
			preparedStatement = connection.prepareStatement("select BID, ASK from quote where symbol='"+ ticker.toUpperCase() +"' " +
					"AND DAY = (select max(DAY) from quote where symbol='"+ticker.toUpperCase()+"' )");
			rs = preparedStatement.executeQuery();

			if(!rs.next()){ //valid symbol but no data
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

			bid = rs.getDouble(1);
			ask = rs.getDouble(2);
			//System.out.println(bid + " " + ask);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//{"bid": 1.23, "ask": 4.56}
		String jsonStr = "{\"bid\": " + Double.toString(bid) +", \"ask\": " + Double.toString(ask) + "}";

		return ResponseEntity.status(HttpStatus.OK).body(jsonStr);
	}

}
