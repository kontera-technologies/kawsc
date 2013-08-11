'use strict';

angular.module('kawsc')
  .controller('InstanceListCtrl', function($scope, $http, $filter, $log) {
    $scope.loading = true;
    $scope.minPrice = 0;

    $scope.visible = function () {
      return _.filter($filter('filter')($scope.instances, $scope.query), function(i) {
        return i.state.name === "running" && i.kona_price >= $scope.minPrice;
      });
    };

    $scope.total$ = function () {
      return _.reduce($scope.visible(), function(acc, i) {
        return acc + i.kona_price;
      }, 0);
    };

    $http.get('/instances')
      .success(function(data) {
        $scope.instances = data;
        $scope.loading = false;
      }).error(function(data, status) {
        $log.error("Failed to retrieve instances", status, data);
      });
  });
