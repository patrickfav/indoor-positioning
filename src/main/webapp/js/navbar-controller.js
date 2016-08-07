(function() {
	'use strict';
	angular.module('adminConsoleApp').controller('NavbarCtrl', ['$scope','$window','$http','navStatus', function($scope,$window,$http,navStatus) {
		$scope.mainMenuIndex = 0;
		$scope.loading = navStatus.getLoading;
		$scope.showErrorIcon= navStatus.getError;
		$scope.status= navStatus.getStatus;
		$scope.idle = navStatus.getIdle;
		$scope.isCollapsed = true;
		$scope.debugLoadingCount = navStatus.getLoadingCount;

		(function init () {
			$scope.hash = $window.location.hash;
		})();

		$scope.$on('$routeChangeSuccess', function(angularEvent, currentRoute, previousRoute) {
			$scope.mainMenuIndex = currentRoute.mainMenuIndex;
		});
	}]);
})();