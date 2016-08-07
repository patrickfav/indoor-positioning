(function() {
	'use strict';

angular.module('adminConsoleApp').controller('NetworkDetailCtrl', ['$scope', '$http', 'navStatus', '$window', '$routeParams', '$modal', '$timeout', function ($scope, $http, navStatus, $window, $routeParams, $modal, $timeout) {
	var pollingTimeMs = 5000;
	var pollingTask = {}, idleTask = {};
	var suspendPolling = false;
	var idleTimeMs = 120000;

	$scope.networkId = "";
	$scope.wrapper = {};
	$scope.jobs = {};
	$scope.serverConfig = {};


	(function init() {
		console.log("init network details");
		$scope.networkId = $routeParams.networkId;
		navStatus.reset();
		reloadConfigData();
		reloadNodesAndNetwork();
		reloadJobs();
		pollingTask = $timeout(tick, pollingTimeMs);
		$window.onblur = function () {
			idleTask = $timeout(function () {
				suspendPolling = true;
				console.log("blur window");
				navStatus.idle(true);
			}, idleTimeMs)
		};
		$window.onfocus = function () {
			$timeout.cancel(idleTask);
			suspendPolling = false;
			console.log("focus window");
			navStatus.idle(false);
		};
	})();

	function tick() {
		if (!navStatus.getLoading() && !suspendPolling && ($scope.serverConfig.autoReloadJobs == undefined || $scope.serverConfig.autoReloadJobs)) {
			console.log("polling jobs");
			reloadJobs();
		}
		pollingTask = $timeout(tick, pollingTimeMs);
	}

	pollingTask.then(
		function () {
			console.log("start polling task", Date.now());
		},
		function () {
			console.log("polling task destroyed", Date.now());
		}
	);

	$scope.$on("$destroy", function (event) {
		$timeout.cancel(pollingTask);
		$timeout.cancel(idleTask);
	});


	function reloadConfigData() {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/config",
			headers: {'If-None-Match': $scope.serverConfig.etag}})
			.success(function (data, status, headers, config) {
				$scope.serverConfig = data;
				$scope.serverConfig.etag = headers("Etag");
				navStatus.endLoading();
				setFloorplanBlueprintUrl();
			}).error(function (data, status) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	function reloadNodesAndNetwork() {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/network/" + $scope.networkId,
			headers: {'If-None-Match': $scope.wrapper.etag}})
			.success(function (data, status, headers, config) {
				$scope.wrapper.nodes = data.nodeList;
				$scope.wrapper.network = data.sensorNetwork;
				$scope.wrapper.roomList = data.roomList;
				$scope.wrapper.etag = headers("Etag");
				setFloorplanBlueprintUrl();
				navStatus.endLoading();
				loadPingLogs();
			}).error(function (data, status, headers, config) {
				if (status == 304) {
					loadPingLogs();
				}
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	function setFloorplanBlueprintUrl() {
		if($scope.serverConfig.couchDBUrl && $scope.wrapper.network) {
			for (var attachmentName in $scope.wrapper.network._attachments) {
				if (attachmentName.indexOf("blueprint") > -1) {
					$scope.wrapper.getFloorplanBlueprintSrc = $scope.serverConfig.couchDBUrl + "/" + $scope.wrapper.network._id + "/" + attachmentName + "?cache-control-rev=" + $scope.wrapper.network._attachments[attachmentName].revpos;
				}
			}
		}
	}

	function loadPingLogs() {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/networkservice/" + $scope.networkId + "/ping",
			headers: {'If-None-Match': $scope.wrapper.etagPing}})
			.success(function (data, status, headers, config) {
				navStatus.endLoading();
				$scope.wrapper.etagPing = headers("Etag");
				for (var i = 0; i < data.length; i++) {
					for (var j = 0; j < data.length; j++) {
						if ($scope.wrapper.nodes[i].nodeId == data[j].nodeId) {
							$scope.wrapper.nodes[i].ping = data[j];
						}
					}
				}
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	function reloadJobs() {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/jobs/network/" + $scope.networkId,
			headers: {'If-None-Match': $scope.jobs.etag}})
			.success(function (data, status, headers, config) {
				navStatus.endLoading();
				$scope.jobs.list = data;
				$scope.jobs.etag = headers("Etag");
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	/* ******************************************************************** SIDE MENU */

	$scope.refresh = function () {
		reloadNodesAndNetwork();
		reloadJobs();
		$scope.$broadcast('reloadAnalysis');
	};

	$scope.switchCronEnabled = function () {
		navStatus.startLoading();
		var enable = !$scope.wrapper.network.cronEnabled;
		$http({
			method: 'POST',
			params: {'enable': enable, 'networkId': $scope.networkId},
			url: constants.getApiUrl() + "/jobs/schedule/config/"
		}).success(function (data) {
			reloadNodesAndNetwork();
			navStatus.endLoading();
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	};

	$scope.scheduleJob = function (type) {
		navStatus.startLoading();
		$http({
			method: 'POST',
			params: {'type': type, 'networkId': $scope.networkId},
			url: constants.getApiUrl() + "/jobs/schedule/"
		}).success(function () {
			navStatus.endLoading();
			reloadJobs();
			reloadNodesAndNetwork();
		}).error(function (data, status) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	};

	/* ******************************************************************** NODE FEATURES */
	$scope.getRoomNameById = function(roomId) {
		if($scope.wrapper.roomList && roomId) {
			for(var i=0;i<$scope.wrapper.roomList.rooms.length;i++) {
				if($scope.wrapper.roomList.rooms[i].roomId == roomId) {
					return $scope.wrapper.roomList.rooms[i].name;
				}
			}
		}
		return "unknown"
	};

	$scope.ping = function (node) {
		navStatus.startLoading();
		$http({
			method: 'POST',
			params: {'persist': true},
			url: constants.getApiUrl() + "/sensor/ping-cgi/",
			data: node
		}).success(function (data) {
			reloadNodesAndNetwork();
			navStatus.endLoading();
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	};

	$scope.compactSurveys = function (nodeId) {
		navStatus.startLoading();
		$http({
			method: 'PUT',
			params: {'nodeId': nodeId, 'keep': $wrapper.network.surveysPerNodeForAnalysis},
			url: constants.getApiUrl() + "/survey/compact"
		}).success(function (data) {
			navStatus.endLoading();
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	};

	/* ******************************************************************** MODALS & CALLBACKS */

	$scope.$on('updateSensorNodesAndNetwork', function (event, data) {
		reloadNodesAndNetwork();
	});

	$scope.openUpdateNetworkModal = function () {
		$modal.open({
			templateUrl: 'partials/modal/network_add.html',
			controller: 'NetworkAddModalCtrl',
			backdrop: 'static',
			scope: $scope
		});
	};

	$scope.openAddNodeModal = function () {
		$modal.open({
			templateUrl: 'partials/modal/node_add.html',
			controller: 'NodeAddModalCtrl',
			backdrop: 'static',
			size: 'lg',
			scope: $scope
		});
	};

	$scope.openBlacklistModal = function () {
		$modal.open({
			templateUrl: 'partials/modal/blacklist_edit.html',
			controller: 'BlacklistModalCtrl',
			backdrop: 'static',
			scope: $scope
		});
	};

	$scope.openPathLossModal = function () {
		$modal.open({
			templateUrl: 'partials/modal/pathloss_edit.html',
			controller: 'PathLossModalCtrl',
			backdrop: 'static',
			size: 'lg',
			scope: $scope
		});
	};
}]);
})();