
var currentPath = window.location.pathname;

$("#ok").click(function () {
    var user = $("#user").val().trim();
    var pass = $("#password").val().trim();
    if (user === "" || pass === ""){
        toastr.error("账号或密码不能为空!");
        return;
    }

    $.ajax({
        type: "post",
        url: currentPath + "api/login",
        dataType: "json",
        data: {
            "user": user,
            "pass": md5(pass)
        },
        success:function (result){
            if (result.success === true){
                toastr.success("数据已提交至服务器，请稍等...");
                qanDao();
            }
            else {
                toastr.error(result.msg);
                //qanDao();//debug用，删
            }
        },
        error:function (err){
            toastr.error("未知错误！")
        }

    });
    
});

function qanDao() {
    $.ajax({
        type: "post",
        url: currentPath + "api/checkin",
        dataType: "json",
        data: {
            "user": $("#user").val().trim(),
            "pass": md5($("#password").val().trim()),
            "longitude": $("#longitude").val().trim(),
            "latitude": $("#latitude").val().trim(),
            "address": $("#address").val().trim(),
            "picture": $("#previewImage").attr("src"),
            "autoCheck": $("#autoCheck").prop("checked")
        },
        success: function (result){
            if (result.success === true){
                toastr.success(result.msg);
            }
            else toastr.error(result.msg);
        },
        error:function (){
            toastr.error("服务器错误!请联系管理员。")
        }
    });
}

$("#picture").change(function() {
    // 获取选择的文件
    var file = this.files[0];

    if (file) {
        // 使用FileReader读取文件内容
        var reader = new FileReader();

        reader.onload = function(e) {

            //图片原始base64
            var base64 = e.target.result;

            //创建新的图像对象
            var img = new Image();

            img.onload = function(){
                var newWidth = 1500;
                var scaleFactor = newWidth / img.width;
                var newHeight = img.height * scaleFactor;

                // 创建一个canvas元素用于绘制缩放后的图像
                var canvas = document.createElement('canvas');
                canvas.width = newWidth;
                canvas.height = newHeight;
                // 获取2D上下文
                var ctx = canvas.getContext('2d');
                // 在canvas上绘制缩放后的图像
                ctx.drawImage(img, 0, 0, newWidth, newHeight);
                // 将canvas转换为base64字符串
                var scaledBase64 = canvas.toDataURL('image/jpeg');

                $("#previewImage").attr("src", scaledBase64).css("display", "block");

            };

            img.src = base64;

        };

        // 以DataURL形式读取文件内容（base64）
        reader.readAsDataURL(file);
    }
});