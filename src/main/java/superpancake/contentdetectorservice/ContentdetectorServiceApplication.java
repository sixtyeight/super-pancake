package superpancake.contentdetectorservice;

import java.io.IOException;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
@Configuration
public class ContentdetectorServiceApplication implements ErrorController {

	@Autowired
	private TikaConfig tika;

	public static void main(String[] args) {
		SpringApplication.run(ContentdetectorServiceApplication.class, args);
	}

	@Bean
	public TikaConfig tikaConfig() {
		try {
			return new TikaConfig();
		} catch (IOException exception) {
			throw new RuntimeException("Tika configuration failed", exception);
		} catch (TikaException exception) {
			throw new RuntimeException("Tika configuration failed", exception);
		}
	}

	@PostMapping(path = "/content", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String getContentType(@RequestBody byte[] payload) {
		try {
			MediaType mediaType = tika.getDetector().detect(TikaInputStream.get(payload), new Metadata());
			return String.format("{ \"mediaType\" : \"%s\" }", mediaType.toString());
		} catch (IOException ioException) {
			throw new RuntimeException("Tika detection failed", ioException);
		}
	}

	private static final String ERROR_PATH = "/error";

	@RequestMapping(value = ERROR_PATH)
	public String error() {
		return "Error handling";
	}

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

}
