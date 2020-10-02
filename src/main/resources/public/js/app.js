var api = (function () {
    var url="https://ec2-3-84-237-39.compute-1.amazonaws.com:8081";
    function serviceData(){
        axios.get(url+"/secured/service").then(res=>{
            $("#serviceResponse").text(res.data);
        })
    }

    function login(){
        var user={email:document.getElementById("email").value,password:document.getElementById("password").value};
        axios.post(url+"/login",user).then(res=>{
            if(res.data!=""){
                alert(res.data)
            }
            else {
                window.location.href="secured/index.html";
            }

        })
    }

    return {
        login:login,
        serviceData:serviceData
    };
})();