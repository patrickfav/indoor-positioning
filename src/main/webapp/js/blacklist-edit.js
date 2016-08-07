'use strict';

angular.module('adminConsoleApp').controller('BlacklistModalCtrl', ['$scope','$http','navStatus','$modalInstance', function($scope, $http, navStatus,$modalInstance) {
	$scope.blacklist = {};

	(function init () {
		loadBlacklist();
	})();

	function loadBlacklist () {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/networkservice/"+$scope.networkId+"/blacklist",
			headers: {'If-None-Match': $scope.blacklist.etag}}
		).success(function (data, status, headers) {
			navStatus.endLoading();
			$scope.blacklist = data;
			$scope.blacklist.etag = headers("Etag");
			}).error(function (data, status, headers, config) {
			navStatus.endLoading();
		});
	}

	function save () {
		navStatus.startLoading();
		$http({
			method: 'PUT',
			url: constants.getApiUrl() + "/networkservice/"+$scope.networkId+"/blacklist",
			data:$scope.blacklist
		}).success(function (data, status, headers) {
			navStatus.endLoading();
			$scope.blacklist = data;
			$scope.blacklist.etag = headers("Etag");
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
		});
	}

	$scope.addMac = function() {
		if($scope.blacklist.addNewMac != undefined && $scope.blacklist.addNewMac.length > 0) {
			if($scope.blacklist.macList == undefined) {
				$scope.blacklist.macList = [];
			}
			$scope.blacklist.macList.push($scope.blacklist.addNewMac);
			$scope.blacklist.addNewMac='';
			save();
		}
	};

	$scope.deleteMac = function(index) {
		$scope.blacklist.macList.splice(index,1);
		save();
	};

	$scope.setActAsWhiteList = function(actAsWhiteList) {
		$scope.blacklist.actAsWhiteList = actAsWhiteList;
		save();
	};

	$scope.close = function () {
		$modalInstance.close();
	};
}]);