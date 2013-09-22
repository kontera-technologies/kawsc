'use strict';

angular.module('kawsc', ['ngRoute', 'ui.bootstrap'])
  .config(function($routeProvider) {
    $routeProvider
      .when('/instances', {controller: 'InstanceListCtrl',
                           pageKey: 'instances',
                           templateUrl: 'templates/instances/list.html'})
      .when('/reservations', {controller: 'ReservationListCtrl',
                              pageKey: 'reservations',
                              templateUrl: 'templates/reservations/list.html'})
      .otherwise({redirectTo: '/instances'});
  }).run(function($rootScope) {
    $rootScope.$on('$routeChangeSuccess', function(ae, curRoute, prevRoute) {
      $rootScope.pageKey = curRoute.pageKey;
    });
  })
  .controller('NavBarCtrl', function($scope) {
    $scope.isActive = function(pageKey) {
      return $scope.$parent.pageKey == pageKey;
    };
  });
