(function() {
	'use strict';

	angular.module('adminConsoleApp').controller('ConfigCtrl', ['$scope', '$http', 'navStatus','$timeout','localStorageService', function ($scope, $http, navStatus,$timeout,localStorageService) {
		$scope.serverConfig = {};

		(function init () {
			reloadConfigData();
		})();

		function reloadConfigData() {
			navStatus.startLoading();
			$http({
				method: 'GET',
				url: constants.getApiUrl()+"/config",
				headers: {'If-None-Match': $scope.serverConfig.etag}})
				.success(function (data, status, headers, config) {
					$scope.serverConfig = data;
					$scope.serverConfig.etag = headers("Etag");
					navStatus.endLoading();
				}).error(function (data, status) {
					navStatus.endLoading();
					navStatus.addError(status,data.statusDescription,data.exceptionList);
				});
		}

        $scope.updateAutoReloadsJobs = function(isOn) {
            $scope.serverConfig.autoReloadJobs = isOn;
            updateConfig(undefined,false);
        };

		$scope.updateSignalMapConfig = function() {
			if($scope.signalStrengthConfigForm.$dirty) {
				updateConfig('signalStrengthConfigForm');
			}
		};

		function updateConfig(formName) {
			navStatus.startLoading();
			$http({
				method: 'POST',
				url: constants.getApiUrl()+"/config",
				data: $scope.serverConfig})
				.success(function (data, status, headers, config) {
					$scope.serverConfig = data;
                    if(formName != undefined) {
                        $scope[formName].$setPristine();
                    }
					$scope.saveIndicator = true;
					$timeout(function () {$scope.saveIndicator = false;}, 3000);
					navStatus.endLoading();
				}).error(function (data, status) {
					navStatus.endLoading();
					navStatus.addError(status,data.statusDescription,data.exceptionList);
			});
		}



		$scope.reset = function() {
			if(confirm("Really Reset? This can not be undone.")) {
				$http.get("/reset").success(function () {
					navStatus.endLoading();
					$window.location.reload();
				}).error(function (data, status, headers, config) {
					navStatus.endLoading();
				});
			}
		};

		$scope.clearLocalStorage = function() {
			localStorageService.clearAll();
		}

    }]);
})();
