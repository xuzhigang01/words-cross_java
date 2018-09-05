package code;

import org.junit.Test;

public class WordsCrossBuilderTest {

    @Test
    public void TestBuild() {
        String words = "ghost,gist,hint,hoist,host,into,might,moist,month,night,omit,shot,"
                + "sigh,sight,sign,sing,smog,sting,thing,tongs";
        WordsCrossBuilder.build(words);
    }
}
