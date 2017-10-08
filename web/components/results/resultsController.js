angular
    .module('ingr3', ["chart.js"])
    .controller("BarCtrl", function ($scope) {
        $scope.labels = ['Orange', 'Shrub', 'Carrots', 'Mint', 'asdfasd', 'asdfasdfa', 'asdfasdfa'];
        $scope.series = ['Health Value'];
        $scope.data = [ [ 1, 0.3, 0.8, -1, -0.2, 0.4, 0.7 ] ];
        $scope.descriptions = [];
    });
