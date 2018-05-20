package superpancake.contentdetectorservice;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.spring.autoconfigure.MeterRegistryCustomizer;

@SpringBootApplication
@Controller
@Configuration
public class ContentdetectorServiceApplication implements ErrorController {

	private static Log LOGGER = LogFactory.getLog(ContentdetectorServiceApplication.class);
	
	@Autowired
	private TikaConfig tika;

	@Autowired
	private Timer uploads;

	@Autowired
	private Counter volume;

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

	public static class ContentTypeResponse {
		public String mediaType;
	}

	@PostMapping(path = "/content", produces = "application/json; charset=UTF-8", consumes = "application/octet-stream")
	@ResponseBody
	public ContentTypeResponse getContentType(@RequestBody byte[] payload) {
		final long started = System.currentTimeMillis();
		try {
			MediaType mediaType = tika.getDetector().detect(TikaInputStream.get(payload), new Metadata());

			ContentTypeResponse r = new ContentTypeResponse();
			r.mediaType = mediaType.toString();

			LOGGER.info("detected '" + r.mediaType + "' (" + payload.length + " bytes)");
			
			return r;
		} catch (IOException ioException) {
			throw new RuntimeException("Tika detection failed", ioException);
		} finally {
			volume.increment(payload.length / 1024 / 1024);
			uploads.record(System.currentTimeMillis() - started, TimeUnit.MILLISECONDS);
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

	@Bean
	MeterRegistryCustomizer<?> meterRegistryCustomizer(MeterRegistry meterRegistry) {
		return meterRegistry1 -> {
			meterRegistry.config().commonTags("application", "content-detector-service");
		};
	}

	@Bean
	Timer uploads(MeterRegistry registry) {
		Timer uploads = registry.timer("uploads");
		return uploads;
	}

	@Bean
	Counter volume(MeterRegistry registry) {
		Counter volume = registry.counter("volume");
		return volume;
	}

}
