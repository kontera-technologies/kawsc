'use strict';

angular.module('kawsc')
  .controller('InstanceListCtrl', function($scope, $http, $log) {

    $scope.running = function () {
      return _.filter($scope.instances, function(i) {
        return i.state.name === "running";
      });
    };

    $http.get('/instances')
      .success(function(data) {
        $log.log(data);
        $scope.instances = data;
      }).error(function(data, status) {
        $log.error("Failed to retrieve instances", status, data);
      });
  });
