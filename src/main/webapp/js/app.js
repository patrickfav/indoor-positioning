'use strict';

var constants = (function(){
	var Constructor = function(){
        this.getApiUrl = function(){return "api";};

		this.getFlatColors=function(){return ['rgba(26, 188, 156,1.0)','rgba(46, 204, 113,1.0)','rgba(52, 152, 219,1.0)','rgba(155, 89, 182,1.0)','rgba(52, 73, 94,1.0)',
			'rgba(22, 160, 133,1.0)','rgba(39, 174, 96,1.0)','rgba(41, 128, 185,1.0)','rgba(142, 68, 173,1.0)','rgba(44, 62, 80,1.0)',
			'rgba(241, 196, 15,1.0)','rgba(230, 126, 34,1.0)','rgba(231, 76, 60,1.0)',
			'rgba(243, 156, 18,1.0)','rgba(211, 84, 0,1.0)','rgba(192, 57, 43,1.0)','rgba(189, 195, 199,1.0)','rgba(127, 140, 141,1.0)']};
	};
    return new Constructor();
})();

(function() {
	'use strict';

	angular.module('adminConsoleApp',['ngRoute',
		'ui.bootstrap','highcharts-ng','sensor-ng','sensor-ng-filter','signalmap-ng','FBAngular','angularFileUpload','angular-ladda','LocalStorageModule','ngSanitize','ngCsv']);

	angular.module('adminConsoleApp').config(['$routeProvider', '$locationProvider','$animateProvider',function($routeProvider, $locationProvider) {
		console.log("initilaize routeProvider");

		$routeProvider.when('/networks', {
			templateUrl: 'partials/networks.html',
			controller: 'NetworksCtrl',
			mainMenuIndex:0
		}).when('/networks/:networkId', {
			templateUrl: 'partials/network-details.html',
			controller: 'NetworkDetailCtrl',
			mainMenuIndex:0
		}).when('/networks/:networkId/node/:nodeId', {
			templateUrl: 'partials/node-details.html',
			controller: 'NodeDetailCtrl',
			mainMenuIndex:0
		}).when('/positioning', {
			templateUrl: 'partials/positioning.html',
			controller: 'PositioningCtrl',
			mainMenuIndex:1
		}).when('/config', {
			templateUrl: 'partials/config.html',
			controller: 'ConfigCtrl',
			mainMenuIndex:2
		}).otherwise ({
			redirectTo: '/networks'
		});
		$locationProvider.html5Mode(false);
	}]);

	angular.module('adminConsoleApp').config(['localStorageServiceProvider',function(localStorageServiceProvider) {
		localStorageServiceProvider.setPrefix('sn');
	}]);
})();