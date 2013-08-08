'use strict';

angular.module('kawsc')
  .filter('filterByMinPrice', function() {
    return function(i, minPrice) {
      return i.kona_price >= $scope.min_price;
    };
  })
  .controller('InstanceListCtrl', function($scope, $http, $log) {
    $scope.loading = true;
    $scope.minPrice = 0;

    $scope.running = function () {
      return _.filter($scope.instances, function(i) {
        return i.state.name === "running" && i.kona_price >= $scope.minPrice;
      });
    };

    $http.get('/instances')
      .success(function(data) {
        //$log.log(data);
        _.each(data, function(i) {
          if (i.instance_type === "cc1.4xlarge") {
            $log.log(i);
          }
        });

        $scope.instances = data;
        $scope.loading = false;
      }).error(function(data, status) {
        $log.error("Failed to retrieve instances", status, data);
      });
  });
