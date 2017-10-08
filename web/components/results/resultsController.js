var myApp = angular.module('ingr3', ["chart.js"]);


myApp.controller("BarCtrl", [ '$scope' , '$http' , function ($scope, $http) {
    $scope.series = ['Health Value'];
    $scope.data = [ [ 1, 0.3, 0.8, -1, -0.2, 0.4, 0.7 ] ];
    $scope.labels = ['Orange', 'Shrub', 'Carrots', 'Mint', 'asdfasd', 'asdfasdfa', 'asdfasdfa'];
    $scope.descriptions = ['lkasjdlfkja', 'lkasjdlfkja', 'lkasjdlfkja', 'lkasjdlfkja', 'lkasjdlfkja', 'lkasjdlfkja', 'lkasjdlfkja'];
    $scope.barChartOptions = {
        showToolTips: false,
        tooltipEvents: ["mousemove", "touchstart", "touchmove"],
        tooltips: {
            callbacks: {
                beforeLabel: function(tooltipItem, data) {
                    return $scope.descriptions[tooltipItem.index];
                },
            }
        }
    };

    $scope.writeFile = function($scope, url) { 
        
        $http.get('http://localhost:8080/work/' + 'url', {})
                .success(function(data){
                        console.log(data);
                });
    }
}]);
