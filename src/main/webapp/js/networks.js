(function() {
'use strict';

angular.module('adminConsoleApp').controller('NetworksCtrl', ['$scope','$http','navStatus','$modal', function($scope,  $http, navStatus,$modal) {
	$scope.networks = {};

	(function init() {
		console.log("init networks");
		navStatus.reset();
		reloadSensorNetworks();
	})();

	function reloadSensorNetworks() {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/network",
			headers: {'If-None-Match': $scope.networks.etag}})
			.success(function (data, status, headers, config) {
				navStatus.endLoading();
				$scope.networks.list = data.networks;
				$scope.networks.pingMap = data.pingMap;
				$scope.networks.etag = headers("Etag");
				setOnlineCounter();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status,data.statusDescription,data.exceptionList);
			});
	}

	function setOnlineCounter() {
		if($scope.networks != undefined) {
			$scope.networks.pingCount ={};
			for(var id in $scope.networks.pingMap) {
				for(var i=0;i<$scope.networks.pingMap[id].length;i++) {
					if($scope.networks.pingCount[id] == undefined) {
						$scope.networks.pingCount[id] = {};
						$scope.networks.pingCount[id].online = 0;
						$scope.networks.pingCount[id].offline = 0;
					}
					if($scope.networks.pingMap[id][i].success) {
						$scope.networks.pingCount[id].online++;
					} else {
						$scope.networks.pingCount[id].offline++;
					}
				}
			}
		}
	}

	$scope.$on('updateSensorNetwork', function(event, data) {reloadSensorNetworks(); });

	$scope.openAddNetworkModal = function () {
        var modalInstance = $modal.open({
            templateUrl: 'partials/modal/network_add.html',
            controller: 'NetworkAddModalCtrl',
            backdrop:'static',
            scope:$scope
        });

        modalInstance.result.then(function (selectedItem) {
            //do smth on select
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };
}]);

})();