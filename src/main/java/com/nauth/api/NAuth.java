package com.nauth.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NAuth {

    private final static Logger logger = LoggerFactory.getLogger(NAuth.class);

    private final String serverURI;
    private final String serverId;
    private final String apiKey;
    private final NAuthBackend backend;
    private final String realm;


    public NAuth(String serverURI, String serverId, String apiKey, String realm, boolean useSSL) {
        this.serverURI = serverURI;
        this.serverId = serverId;
        this.apiKey = apiKey;
        this.realm = realm;

        backend = new NAuthBackend(useSSL);
    }

    public NAuth(String serverURI, String serverId, String apiKey, String realm) {
        this(serverURI, serverId, apiKey, realm, false);
    }

    private String serverGet(String method, String[] queryParts, Map<String, String> params) {
        return backend.getHttpAsString(method, serverURI, queryParts, params, getHeaders());
    }

    private byte[] serverGetBytes(String method, String[] queryParts, Map<String, String> params) {
        return backend.getHttpAsBytes(method, serverURI, queryParts, params, getHeaders());
    }


    /**
     * Logout the current NAuth session
     */
    public void logout(String sessionId) {
        serverGet("POST", new String[]{"servers", serverId, "sessions", sessionId, "logout"}, null);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-ApiKey", apiKey);
        return headers;
    }

    private JSONObject sessionCheck(final String sessionId) {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(serverGet("GET",
                    new String[]{"servers", serverId, "sessions", sessionId}, params));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Fetches either the information about the logged in user or the QR code to login
     *
     * @return @see com.nauth.api.LoginInformation object that contains information about the login
     */
    public LoginInformation tryLogin(String sessionId) {
        JSONObject loginData = sessionCheck(sessionId);
        if (loginData == null) return null;
        return new LoginInformation(
                (Boolean) loginData.get("loggedin"),
                (Boolean) loginData.get("canprovoke"),
                (String) loginData.get("userid"),
                (String) loginData.get("loginqrdata"),
                (String) loginData.get("pk"),
                (String) loginData.get("hsid")
        );
    }

    /**
     * Register a userId for the currently logged in session
     *
     * @param userId
     * @return boolean True on success
     */
    public boolean registerUser(String sessionId, String userId) {
        if (!tryLogin(sessionId).isLoggedIn())
            return false;

        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("userid", userId);

        serverGet("POST",
                new String[]{"servers", serverId, "sessions", sessionId, "registeruser"}, params);
        return true;
    }

    /**
     * Returns a PNG image of the visual login code
     *
     * @param sessionId the id of the session
     * @param imgtype   Type of image
     * @param size      Size of the image in pixels
     * @return Raw PNG data
     */
    public byte[] getLoginImage(String sessionId, ImageType imgtype, int size) {
        if (imgtype == null)
            imgtype = ImageType.QR;

        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("userid", null);
        params.put("type", RequestType.LOGIN.toString());
        params.put("name", null);
        params.put("s", "" + size);
        params.put("img", imgtype.toString());

        return serverGetBytes("GET", new String[]{"servers", serverId, "sessions", sessionId, "qr"}, params);
    }

    /**
     * Returns a PNG image of the visual register code
     *
     * @param sessionId the id of the session
     * @param userId    the id of the user trying to register
     * @param userId    the name of the user trying to register
     * @param size      Size of the image in pixels
     * @return Raw PNG data
     */
    public byte[] getRegisterImage(String sessionId, String userId, String name, int size) {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("userid", userId);
        params.put("type", RequestType.REGISTER.toString());
        params.put("name", name);
        params.put("s", size + "");
        params.put("img", ImageType.QR.toString());

        return serverGetBytes("GET", new String[]{"servers", serverId, "sessions", sessionId, "qr"}, params);
    }

    /**
     * Provoke a login for the current user
     *
     * @return boolean True on success
     */
    public boolean provokelogin(String sessionId) {
        String result = serverGet("POST", new String[]{"servers", serverId, "sessions", sessionId, "provokelogin"}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            return ret;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all accounts of the specified user
     *
     * @param userid Userid
     * @return JSONArray array of JSONObject's containing account data
     */
    public List<NAuthAccount> getUserAccountsFor(String userid) {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        String data = serverGet("GET", new String[]{"servers", serverId, "users", userid}, params);

        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray accounts = (JSONArray) jsonParser.parse(data);
            return convertToAccounts(accounts);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<NAuthAccount> convertToAccounts(JSONArray accounts) {
        List<NAuthAccount> nAuthAccounts = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            nAuthAccounts.add(convertToAccount((JSONObject) accounts.get(i)));
        }
        return nAuthAccounts;
    }

    private NAuthAccount convertToAccount(JSONObject account) {
        return new NAuthAccount(
                (Long) account.get("id"),
                (Boolean) account.get("publicKeyAuthRevoked"),
                (Boolean) account.get("publicKeyTransRevoked"),
                (String) account.get("description"),
                new Date((Long) account.get("lastlogin")),
                new Date((Long) account.get("creation")),
                (Boolean) account.get("blocked")
        );
    }

    /**
     * Get all accounts of all users
     *
     * @return JSONArray array of JSONObject's containing account data
     */
    public JSONArray getUsers() {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        String data = serverGet("GET", new String[]{"servers", serverId, "users"}, params);

        JSONParser jsonParser = new JSONParser();
        try {
            return (JSONArray) jsonParser.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * block account of the given user
     *
     * @param userid Userid
     * @return true if the action was successful
     */
    public void blockUser(String userid, boolean blocked) {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("blocked", blocked ? "true" : "false");

        serverGet("PUT", new String[]{"servers", serverId, "users", userid}, params);
    }

    /**
     * Create a new transaction with the specified message
     *
     * @param sessionId The id of the session
     * @param msg       Message for the transaction
     * @return Base64 encoded transaction identifier
     */
    public String createTransaction(String sessionId, String msg) {
        Map<String, String> params = new HashMap<>();
        params.put("msg", msg);

        String result = serverGet("POST", new String[]{"servers", serverId, "sessions", sessionId, "transactions"}, params);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            if (ret == true) {
                return (String) obj.get("tid");
            }

            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check the status of the transaction
     *
     * @param sessionId     The id of the session
     * @param transactionId Base64 encoded transaction identifier
     * @return 0 for new transaction, 1 for approved transaction, 2 for declined transactions or null if the transaction does not exist
     */
    public Integer checkTransaction(String sessionId, String transactionId) {
        String result = serverGet("GET", new String[]{"servers", serverId, "sessions", sessionId, "transactions", transactionId}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            if (ret == true) {
                return (Integer) obj.get("tstatus");
            }

            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the image that identifies the n-auth server.
     *
     * @return The raw png data that is the visual hash of the server
     */
    public byte[] getVashImage() {
        return serverGetBytes("GET", new String[]{"servers", serverId, "vash"}, new HashMap<>());
    }
}
