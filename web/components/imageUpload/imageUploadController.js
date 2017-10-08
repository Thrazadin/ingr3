angular
    .module('ingr3', [])
    .controller('ImageUploadController', function ($scope, fileUploadService) {
        console.log("got here");
        $scope.uploadFile = function () {
            var appletAttributes = {code:''};
            var appletParameters = {fileLocation: $scope.myFile};
            var javaVersion = '1.8';
            //deployJava.runApplet(appletAttributes, appletParameters, javaVersion);
            console.log("This is running");
            console.log($scope.myFile);
    };
});
