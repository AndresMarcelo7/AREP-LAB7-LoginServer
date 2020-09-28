package edu.eci.arep.SecureSparkApp;

/**
 * Hello world!
 *
 */
import com.google.gson.Gson;
import edu.eci.arep.SecureSparkApp.model.User;
import spark.staticfiles.StaticFilesConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;
public class SecureSparkServices {

    public static void main(String[] args) {
        port(getPort());
        //API: secure(keystoreFilePath, keystorePassword, truststoreFilePath,truststorePassword);
        //Se le dice a Spark: Utilice estos certificados y en particular utilice este certificado:
        //secure("keystores/ecikeystore.p12", "123456", null, null);

        Map<String,String> users=new HashMap<>();
        users.put("prueba@mail.com",hashPassword("123456"));
        secure("keystores/ecikeystore.p12","123456",null,null);
        Gson gson=new Gson();

        before("protected/*", (req, response) ->{
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
                response.redirect("/index.html");
            }}));

        StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
        staticHandler.configure("/public");
        before((request, response) ->
                staticHandler.consume(request.raw(), response.raw()));

        get("protected/user",((req, res) -> {
            return req.session().attribute("User");
        }));

        post("/login",((request, response) -> {
            request.body();
            request.session(true);
            User user= gson.fromJson(request.body(),User.class);
            if(hashPassword(user.getPassword()).equals(users.get(user.getEmail()))){
                request.session().attribute("User",user.getEmail());
                request.session().attribute("Loged",true);
            }
            else{
                return "Error : Usuario o contraseña incorrecta";
            }
            return "";

        }));



    }
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

    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 5000; //returns default port if heroku-port isn't set(i.e on localhost)
    }

}