package app.drool.respite;

import org.junit.Test;

import app.drool.respite.handlers.LinkHandler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkHandlerUnitTest {
    String url1 = "https://www.reddit.com/r/todayilearned/comments/4opcx2/til_that_in_1911_a_lone_man_emerged_from_the/";
    String url2 = "https://www.reddit.com/r/todayilearned/comments/4opcx2/til_that_in_1911_a_lone_man_emerged_from_the/d4es73e";
    String url3 = "https://www.reddit.com/r/todayilearned/";
    String url4 = "https://www.reddit.com/user/Zantazi";
    String url5 = "http://np.reddit.com/r/autotldr/comments/31bfht/theory_autotldr_concept/";


    @Test
    public void isSubreddit_isCorrect() throws Exception {
        assertNull(LinkHandler.isSubreddit(url1));
        assertNull(LinkHandler.isSubreddit(url2));
        assertNotNull(LinkHandler.isSubreddit(url3));
        assertNull(LinkHandler.isSubreddit(url4));
        assertNull(LinkHandler.isSubreddit(url5));
    }

    @Test
    public void isComment_isCorrect() throws Exception {
        assertNull(LinkHandler.isComment(url1));
        assertNotNull(LinkHandler.isComment(url2));
        assertNull(LinkHandler.isComment(url3));
        assertNull(LinkHandler.isComment(url4));
        assertNull(LinkHandler.isComment(url5));
    }

    @Test
    public void isUser_isCorrect() throws Exception {
        assertNull(LinkHandler.isUser(url1));
        assertNull(LinkHandler.isUser(url2));
        assertNull(LinkHandler.isUser(url3));
        assertNotNull(LinkHandler.isUser(url4));
        assertNull(LinkHandler.isUser(url5));
    }


    @Test
    public void isSubmission_isCorrect() throws Exception {
        assertNotNull(LinkHandler.isSubmission(url1));
        assertNull(LinkHandler.isSubmission(url2));
        assertNull(LinkHandler.isSubmission(url3));
        assertNull(LinkHandler.isSubmission(url4));
        assertNotNull(LinkHandler.isSubmission(url5));
    }
}