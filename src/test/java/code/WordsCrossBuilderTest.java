package code;

import org.junit.Test;

public class WordsCrossBuilderTest {

	@Test
	public void TestBuild() {
		String words = "toe,rote,rose,roster,terse,ore,sere,tree,restore,sort,sore,steer,roe,rot,resort,rest,see,set,stereo,store";
		WordsCrossBuilder.build(words);
	}
}
