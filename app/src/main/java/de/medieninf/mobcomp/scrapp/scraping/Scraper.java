package de.medieninf.mobcomp.scrapp.scraping;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.rest.model.Action;
import de.medieninf.mobcomp.scrapp.rest.model.ActionParam;
import de.medieninf.mobcomp.scrapp.rest.model.Result;
import de.medieninf.mobcomp.scrapp.util.Config;
import de.medieninf.mobcomp.scrapp.util.HashGenerator;
import de.medieninf.mobcomp.scrapp.util.NoSSLv3Factory;
import de.medieninf.mobcomp.scrapp.util.exceptions.ParseException;

/**
 * Scrape a Website or several Websites step by step in order by given actions.
 * Redirects are followed (301 and HTML redirects).
 */
public class Scraper {
    private static final String TAG = Scraper.class.getSimpleName();

    /**
     * Avoid SSLv3 as the only protocol available.
     * http://stackoverflow.com/questions/26633349/disable-ssl-as-a-protocol-in-httpsurlconnection
     * http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests/2793153#2793153
     */
    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
    }
    /**
     * Database Helper to save the current scraping status.
     */
    private DBHelper dbHelper;
    /**
     * List of Actions which are processed step by step.
     */
    private List<Action> actions;

    /**
     * Cookies of Websites, which get visited by crawling.
     */
    private HashMap<String, String> cookies;

    /**
     * Complete document with HTML of current Action.
     */
    private Document document;



    public Scraper(List<Action> actions) {
        this.actions = actions;
        this.cookies = new HashMap<>();
    }

    public Scraper(DBHelper dbHelper, List<Action> actions) {
        this(actions);
        this.dbHelper = dbHelper;
    }


    /**
     * Scrape step by step by given Actions.
     * A clicking user is simulated with Jsoup and different steps to different urls.
     * Cookies and other request specific Data are transferred.
     *
     * @param internalRequestId the status of scraping is updated in to this id
     * @return Result with hashed content and original content
     * @throws IOException
     * @throws ParseException
     */
    public Result scrape(long internalRequestId) throws IOException, ParseException {
        String parsedHtml = null;
        String parsedDisplayHtml = null;
        Parser parser = new Parser();

        // iterate over all actions in order by position
        for (int i=0; i<actions.size(); i++) {
            Action action = actions.get(i);
            Log.v(TAG, action.toString());


            String url = getUrl(parsedHtml, action.getUrl());
            Log.v(TAG, "Action " + (i+1) +"/"+actions.size() + " -- Sending Request to url: " + url);

            // make request with data, cookies, current method
            makeJsoupRequest(getRequestData(action.getActionParams()), getHttpMethodEnum(action.getMethod()), url);


            // set parsedHtml for each Action back to null -> clean start and check at the end
            parsedHtml = null;
            parsedDisplayHtml = null;

            // if a parse expression is set -> parse Content
            if (action.getParseExpression() != null) {
                parsedHtml = parser.parse(document, action.getParseExpression(), action.getParseType());
                Log.v(TAG, "parsedHtml inner loop: " + parsedHtml);
            }

            // if a parse expression for display is set -> parse Content
            if(action.getParseExpressionDisplay() != null) {
                parsedDisplayHtml = parser.parse(document, action.getParseExpressionDisplay(), action.getParseTypeDisplay());
                Log.v(TAG, "Parse to Display value: " + parsedDisplayHtml);
            }

            // save current scraping status
            saveStatus(internalRequestId, i+1);
        }

        // finished walk through website -> create result
        return createResult(parsedHtml, parsedDisplayHtml);
    }

    /**
     * Scrape step by step by given Actions.
     * This scraping is especially for the Web view.
     *
     * @return url of last visited Action
     * @throws IOException
     * @throws ParseException
     */
    public String scrapeForBrowser() throws IOException, ParseException {
        String parsedHtml = null;
        String url = null;
        Parser parser = new Parser();


        // iterate over all actions in order by position
        for (int i=0; i<actions.size(); i++) {
            Action action = actions.get(i);

            url = getUrl(parsedHtml, action.getUrl());
            // if last iteration -> return url so it can be used in web view
            if (i == actions.size()-1) {
                return url;
            }
            Log.v(TAG, "Action " + (i + 1) + "/" + actions.size() + " -- Sending Request to url: " + url);

            // make request with data, cookies, current method
            makeJsoupRequest(getRequestData(action.getActionParams()), getHttpMethodEnum(action.getMethod()), url);

            // set parsedHtml for each Action back to null -> clean start and check at the end
            parsedHtml = null;

            // if a parse expression is set -> parse Content
            if (action.getParseExpression() != null) {
                parsedHtml = parser.parse(document, action.getParseExpression(), action.getParseType());
                //Log.v(TAG, "parsedHtml inner loop: " + parsedHtml);
            }
        }

        // finished walk through website -> return url
        return url;
    }

    /**
     * Makes request to the given url with given request data and method.
     * Follows redirects (including HTML redirects).
     *
     * @param requestData Map of key value pairs for request
     * @param method Connection.Method - HTTP method
     * @param url url as string
     * @throws IOException if there is an error connecting to the url
     */
    private void makeJsoupRequest(Map<String, String> requestData, Connection.Method method, String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .data(requestData)
                .cookies(cookies)
                .referrer("http://www.google.com") // some websites block without referrer
                .userAgent(Config.BROWSER_USER_AGENT) // set explicit user agent for reuse in webview
                .method(method)
                .timeout(15000)
                //.validateTLSCertificates(false) // do not validate ssl certificates -> must be used for self certified
                .execute();

        // get cookies from response and add to all cookies
        addNewCookies(response.cookies());

        // get content from response
        document = response.parse();
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // TODO: check if it works
        //Log.v(TAG, document.toString());

        // check if there is a redirect via meta-equiv=refresh
        Element metaRefresh = document.select("html head meta[http-equiv=refresh]").first();
        if (metaRefresh != null) {
            // get url to redirect
            String refreshContent = metaRefresh.attr("content");
            if (refreshContent != null) {
                // split at ;URL= to get url
                String redirectUrl = refreshContent.split(";URL=")[1];
                if (redirectUrl != null) {
                    // if its a relative redirect url -> prepend with host
                    if (!redirectUrl.toLowerCase().startsWith("http")) {
                        redirectUrl = response.url().toString().concat(redirectUrl);
                    }
                    makeJsoupRequest(requestData, Connection.Method.GET, redirectUrl);
                }
            }
        }
    }

    /**
     * Creates result by given Strings of parsed HTML and parsed display HTML.
     * Evaluates which one is set to Content of the result.
     * Parsed HTML is used to generate the Hash of the result.
     *
     * @param parsedHtml HTML as String, if null -> error
     * @param parsedDisplayHtml HTML as String, may be null
     * @return result
     * @throws ParseException if there couldn't be parsed anything
     */
    private Result createResult(String parsedHtml, String parsedDisplayHtml) throws ParseException {
        // if parsed html == null -> there was an error
        if (parsedHtml == null || parsedHtml.trim().isEmpty()) {
            throw new ParseException("Could not parse any Content!");
        }

        // create result
        Result result = new Result();

        String hash = new HashGenerator().getSha1Hash(parsedHtml);
        result.setHash(hash);
        result.setLastModified(new Date());

        // if parse Expression for Display is given -> this is the Content
        if (parsedDisplayHtml != null) {
            result.setContent(parsedDisplayHtml);
        } else {
            result.setContent(parsedHtml);
        }
        //Log.v(TAG, "Content: " + parsedHtml);
        return result;
    }

    /**
     * Evaluates the url.
     * If an actionUrl is given it is used,
     * if not the parsed HTML of previous action is used.
     *
     * @param parsedHtml parsed HTML of previous action
     * @param actionUrl url of action, may be null
     * @return url as string
     */
    private String getUrl(String parsedHtml, String actionUrl) {
        // if url of action == null -> use parse result of previous action
        if (actionUrl == null) {
            return parsedHtml;
        }
        return actionUrl;
    }

    /**
     * Creates a map with key value data of action params for request.
     *
     * @param actionParams action params of current action
     * @return Map of key value pairs for request
     */
    private Map<String, String> getRequestData(List<ActionParam> actionParams) {
        Map<String, String> requestData = new HashMap<>();
        if (actionParams != null) {
            // iterate over all ActionParams and add params to Map
            for (ActionParam actionParam : actionParams) {
                requestData.put(actionParam.getKey(), actionParam.getValue());
            }
        }
        return requestData;
    }

    /**
     * Saves new cookies to cookies map.
     *
     * @param newCookies Cookies to add
     */
    private void addNewCookies(Map<String, String> newCookies) {
        // iterate over new map and add all new cookies to cookie-Map
        for (Map.Entry<String, String> entry : newCookies.entrySet()) {
            if (entry.getValue() != null) {
                cookies.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Evaluates the HTTP method as enum from the given string.
     * Defaults to GET.
     *
     * @param methodString HTTP method as String
     * @return Connection.Method - HTTP method
     */
    private Connection.Method getHttpMethodEnum(String methodString) {
        Connection.Method method;
        switch (methodString) {
            case "GET":
                method = Connection.Method.GET;
                break;
            case "POST":
                method = Connection.Method.POST;
                break;
            default:
                method = Connection.Method.GET;
                break;
        }
        return method;
    }

    /**
     * Saves the current parsing status to the database.
     *
     * @param currentActionNo number of current action
     */
    private void saveStatus(long internalResultId, int currentActionNo) {
        if (dbHelper != null) {
            dbHelper.updateResultAtParsing(internalResultId, currentActionNo);
        }
    }

    /**
     * Getter for the cookies of the actions.
     * @return Map of cookies.
     */
    public HashMap<String, String> getCookies() {
        return cookies;
    }
}
