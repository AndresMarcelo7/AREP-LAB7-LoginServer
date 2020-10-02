package edu.eci.arep.SecureSparkApp;

/**
 * Hello world!
 *
 */
import com.google.gson.Gson;
import edu.eci.arep.SecureSparkApp.model.User;
import edu.eci.arep.SecureSparkApp.urlutils.HTTPRequest;
import spark.Filter;
import spark.staticfiles.StaticFilesConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * The type Secure spark services (LOGIN SERVICE AND REQUEST).
 */
public class SecureSparkServices {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        port(getPort());

        //Se le dice a Spark: Utilice estos certificados y en particular utilice este certificado:
        //secure("keystores/ecikeystore.p12", "123456", null, null);

        Map<String,String> users=new HashMap<>();
        users.put("marcelo@mail.com",hashPassword("marcelo123"));
        //API: secure(keystoreFilePath, keystorePassword, truststoreFilePath,truststorePassword);
        secure("keystores/ecikeystoreA.p12","123456",null,null);
        Gson gson=new Gson();
        StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
        staticHandler.configure("/public");
        HTTPRequest.enableTrustStore();

        get("/hello",((req, res) -> "Hello World! :D"
        ));

        before("secured/*", (req, response) ->{
            req.session(true);
            if(req.session().isNew()){
                req.session().attribute("Loged",false);
            }
            boolean auth=req.session().attribute("Loged");
            if(!auth){
                halt(401, "<h1>401 Unauthorized</h1>");
            }});

        before("/login.html",((req, response) ->{
            req.session(true);
            if(req.session().isNew()){
                req.session().attribute("Loged",false);
            }
            boolean auth=req.session().attribute("Loged");
            if(auth){
                response.redirect("secured/index.html");
            }}));
        before((request, response) ->
                staticHandler.consume(request.raw(), response.raw()));

        post("/login",((request, response) -> {
            request.body();
            request.session(true);
            User user= gson.fromJson(request.body(),User.class);
            if(hashPassword(user.getPassword()).equals(users.get(user.getEmail()))){
                request.session().attribute("Loged",true);
            }
            else{
                return "Error : Usuario o contraseña incorrecta";
            }
            return "";
        }));
        get("secured/service",(request, response) -> {
            try{
                return HTTPRequest.getRequest();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return "Watefok bro";
        });
    }

    /**
     * Hash a password.
     *
     * @param password the password
     * @return the hash  string
     */
    public static String hashPassword(String password){
        String passwordToHash = password;
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch ( NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 5000; //returns default port if heroku-port isn't set(i.e on localhost)
    }

}