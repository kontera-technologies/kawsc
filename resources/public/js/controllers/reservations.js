'use strict';

angular.module('kawsc')
  .controller('ReservationListCtrl', function($scope, $http, $filter, $log) {
    $scope.loading = true;

    $scope.visible = function () {
      return $filter('filter')($scope.reservations, $scope.query);
    };

    $scope.total$ = function () {
      return _.reduce($scope.visible(), function(acc, r) {
        return acc + r.fixed_price * r.instance_count;
      }, 0);
    };

    $http.get('/reservations')
      .success(function(data) {
        $scope.reservations = data;
        $scope.loading = false;
      }).error(function(data, status) {
        $log.error("Failed to retrieve reservations", status, data);
      });
  });
