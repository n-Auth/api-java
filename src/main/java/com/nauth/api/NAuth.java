package com.nauth.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class NAuth {

    private final String serverURI;
    private final String serverId;
    private final String apiKey;
    private final NAuthBackend backend;
    private final String realm;

    public NAuth(String serverURI, String serverId, String apiKey, String realm, boolean useSSL, String pk, List<String> ciphers) throws NAuthServerException {
        this.serverURI = serverURI;
        this.serverId = serverId;
        this.apiKey = apiKey;
        this.realm = realm;

        if(useSSL)
        	backend = new NAuthBackend(pk, ciphers);
        else
        	backend = new NAuthBackend();
    }

    public NAuth(String serverURI, String serverId, String apiKey, String realm) throws NAuthServerException {
        this(serverURI, serverId, apiKey, realm, false, null, null);
    }

    private String serverGet(String method, String[] queryParts, Map<String, String> params) throws NAuthServerException {
        return backend.getHttpAsString(method, serverURI, queryParts, params, getHeaders());
    }

    private byte[] serverGetBytes(String method, String[] queryParts, Map<String, String> params) throws NAuthServerException {
        return backend.getHttpAsBytes(method, serverURI, queryParts, params, getHeaders());
    }


    /**
     * Logout the current NAuth session
     */
    public void logout(String sessionId) throws NAuthServerException {
        serverGet("POST", new String[]{"servers", serverId, "sessions", sessionId, "logout"}, null);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-ApiKey", apiKey);
        return headers;
    }

    private JSONObject sessionCheck(final String sessionId) throws NAuthServerException {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        JSONParser parser = new JSONParser();
        try {
            return (JSONObject) parser.parse(serverGet("GET",
                    new String[]{"servers", serverId, "sessions", sessionId}, params));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches either the information about the logged in user or the QR code to login
     *
     * @return @see com.nauth.api.LoginInformation object that contains information about the login
     */
    public LoginInformation tryLogin(String sessionId) throws NAuthServerException {
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
    public boolean registerUser(String sessionId, String userId) throws NAuthServerException {
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
     * Returns a PNG image of the visual login code, or the raw data if imgtype == RAWDATA
     *
     * @param sessionId the id of the session
     * @param imgtype   Type of image
     * @param size      Size of the image in pixels
     * @return Raw PNG data
     */
    public byte[] getLoginImage(String sessionId, ImageType imgtype, int size) throws NAuthServerException {
        if (imgtype == null)
            imgtype = ImageType.QR;

        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("userid", null);
        params.put("type", RequestType.LOGIN.toString());
        params.put("name", null);
        params.put("s", "" + size);
        if(imgtype != ImageType.RAWDATA){
        	params.put("img", imgtype.toString());
        }

        return serverGetBytes("GET", new String[]{"servers", serverId, "sessions", sessionId, "qr"}, params);
    }
    

    /**
     * Returns a PNG image of the visual register code, or the raw data if imgtype == RAWDATA
     *
     * @param sessionId the id of the session
     * @param userId    the id of the user trying to register
     * @param userId    the name of the user trying to register
     * @param size      Size of the image in pixels
     * @return Raw PNG data
     */
    public byte[] getRegisterImage(ImageType imgtype, String sessionId, String userId, String name, int size) throws NAuthServerException{
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("userid", userId);
        params.put("type", RequestType.REGISTER.toString());
        params.put("name", name);
        params.put("s", size + "");
        if(imgtype != ImageType.RAWDATA){
        	params.put("img", ImageType.QR.toString());
        }

        return serverGetBytes("GET", new String[]{"servers", serverId, "sessions", sessionId, "qr"}, params);
    }
    
    public byte[] getRegisterImage(String sessionId, String userId, String name, int size) throws NAuthServerException{
    	return getRegisterImage(ImageType.QR,sessionId,userId,name,size);
    }

    /**
     * Provoke a login for the current user
     *
     * @return boolean True on success
     */
    public boolean provokeloginOnSession(String sessionId) throws NAuthServerException{
        String result = serverGet("POST", new String[]{"servers", serverId, "sessions", sessionId, "provokelogin"}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Provoke a login for the current user
     *
     * @return boolean True on success
     */
    public boolean provokeloginOnAccount(String sessionId, String accountId) throws NAuthServerException{
        String result = serverGet("POST", new String[]{"servers", serverId, "accounts", accountId, "provokelogin", sessionId, "provokelogin"}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Provoke a login for the current user
     *
     * @return boolean True on success
     */
    public boolean provokeloginOnUser(String sessionId, String userId) throws NAuthServerException{
        String result = serverGet("POST", new String[]{"servers", serverId, "users", userId, "provokelogin", sessionId, "provokelogin"}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            return ret;
        } catch (Exception e) {
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
    public List<NAuthAccount> getUserAccountsFor(String userid) throws NAuthServerException{
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        String data = serverGet("GET", new String[]{"servers", serverId, "users", userid}, params);

        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray accounts = (JSONArray) jsonParser.parse(data);
            return convertToAccounts(accounts);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<NAuthAccount> convertToAccounts(JSONArray accounts) {
        if (accounts == null) return new ArrayList<>();

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
                new Date((Long) account.get("created")),
                (Boolean) account.get("blocked")
        );
    }

    /**
     * Get all accounts of all users
     *
     * @return JSONArray array of JSONObject's containing account data
     */
    public JSONArray getUsers() throws NAuthServerException {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        String data = serverGet("GET", new String[]{"servers", serverId, "users"}, params);

        JSONParser jsonParser = new JSONParser();
        try {
            return (JSONArray) jsonParser.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    /**
     * block account of the given user
     *
     * @param userid Userid
     * @return true if the action was successful
     */
    public boolean blockAccount(String userid, boolean blocked) throws NAuthServerException {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);
        params.put("blocked", blocked ? "true" : "false");

        serverGet("PUT", new String[]{"servers", serverId, "users", userid}, params);
        return true;
    }

    /**
     * Delete account with given accountId.
     *
     * @param accountId The id of the account that is to be deleted
     */
    public boolean deleteAccount(Long accountId) throws NAuthServerException {
        Map<String, String> params = new HashMap<>();
        params.put("realm", realm);

        serverGet("DELETE", new String[]{"servers", serverId, "accounts", String.valueOf(accountId)}, params);
        return true;
    }

    /**
     * Create a new transaction with the specified message
     *
     * @param sessionId The id of the session
     * @param msg       Message for the transaction
     * @return Base64 encoded transaction identifier
     */
    public String createTransaction(String sessionId, String msg) throws NAuthServerException {
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
        } catch (Exception e) {
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
    public Integer checkTransaction(String sessionId, String transactionId) throws NAuthServerException {
        String result = serverGet("GET", new String[]{"servers", serverId, "sessions", sessionId, "transactions", transactionId}, null);
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(result);
            Boolean ret = (Boolean) obj.get("result");
            if (ret == true) {
                return (Integer) obj.get("tstatus");
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the image that identifies the n-auth server.
     *
     * @return The raw png data that is the visual hash of the server
     */
    public byte[] getVashImage() throws NAuthServerException {
        return serverGetBytes("GET", new String[]{"servers", serverId, "vash"}, new HashMap<>());
    }
}
