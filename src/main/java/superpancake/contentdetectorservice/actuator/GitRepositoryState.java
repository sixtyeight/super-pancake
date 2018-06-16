package superpancake.contentdetectorservice.actuator;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.ALWAYS)
public class GitRepositoryState {

	public Properties git;

}
