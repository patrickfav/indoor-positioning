(function () {
	'use strict';

	angular.module('adminConsoleApp').controller('PositioningCtrl', ['$scope', '$http', 'navStatus', 'localStorageService', 'signalmapApi', 'signalmapCallbacks','$timeout', function ($scope, $http, navStatus, localStorageService, signalmapApi, signalmapCallbacks,$timeout) {

		$scope.navStatus = navStatus;
		$scope.uri = {};
		$scope.uri.ip = '';
		$scope.uri.positioningLoading = false;
		$scope.client = {};
		$scope.sensorInfo = {};
		$scope.measurement = undefined;
		$scope.fittingNetworks = undefined;
		$scope.selectedNetwork = undefined;
		$scope.probablePositionsWrapper = {};
		$scope.mainLoader = {};
		$scope.serverConfig = {};
		/*$scope.fittingNetworks = [
		 {networkName:'Ottakringer Bla',description:'A lightweight, extensible directive for fancy popover creation. The popover directive supports multiple placements, optional transition animation, and more.'},
		 {networkName:'Argentinierstra',description:'A lightweight, extensible directive for fancy popover creation. The popover directive supports multiple placements, optional transition animation, and more.'},
		 {networkName:'Halle X',description:'A lightweight, extensible directive for fancy popover creation. The popover directive supports multiple placements, optional transition animation, and more.'}
		 ];

		 $scope.probablePositions = [
		 {x:8,y:10},
		 {x:44,y:87},
		 {x:67,y:10}
		 ];*/

		(function init() {
			console.log("init positioning");
			navStatus.reset();
			reloadConfigData();
			loadSavedDataOrDefaults();
			refreshCanvas(false);
		})();

		function reloadConfigData() {
			navStatus.startLoading();
			$http({
				method: 'GET',
				url: constants.getApiUrl() + "/config",
				headers: {'If-None-Match': $scope.serverConfig.etag}
			})
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

		function loadSavedDataOrDefaults() {
			if (localStorageService.get("positioningIp") != null) {
				$scope.uri.ip = localStorageService.get("positioningIp");
				checkIpInternal();
			}

			if (localStorageService.get("positioningmeasurmentCount") != null) {
				$scope.client.measurmentCount = localStorageService.get("positioningmeasurmentCount");
			} else {
				$scope.client.measurmentCount = 5;
			}
			if (localStorageService.get("positioningDelay") != null) {
				$scope.client.delay = localStorageService.get("positioningDelay");
			} else {
				$scope.client.delay = 500;
			}
			if (localStorageService.get("positioningMulti") != null) {
				$scope.client.multi = localStorageService.get("positioningMulti");
			} else {
				$scope.client.multi = 1;
			}
			if (localStorageService.get("positioningShouldIncludeExtendedNodes") != null) {
				$scope.client.shouldIncludeExtendedNodes = JSON.parse(localStorageService.get("positioningShouldIncludeExtendedNodes"));
			} else {
				$scope.client.shouldIncludeExtendedNodes = true;
			}
		}

		$scope.checkIp = function () {
			localStorageService.set("positioningIp", $scope.uri.ip);
			checkIpInternal();
		};

		function checkIpInternal() {
			if ($scope.uri.ip.length > 6) {
				pingNode(createNodeFromInput());
			}
		}

		function createNodeFromInput() {
			var node = {};
			var ipElem = $scope.uri.ip.split(":");
			node.ip = ipElem[0];
			node.port = ipElem[1];
			node.httpsEnabled = false;
			return node;
		}

		function pingNode(node) {
			navStatus.startLoading();

			$scope.uri.known = false;
			$scope.uri.loading = true;

			$http({
				method: 'POST',
				params: {'persist': false},
				url: constants.getApiUrl() + "/sensor/ping-cgi/",
				data: node
			}).success(function (data) {
				$scope.uri.pingSuccess = data.success;
				$scope.uri.loading = false;
				$scope.uri.known = true;
				if ($scope.uri.pingSuccess) {
					loadSensorInfo(node);
				}
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				$scope.uri.pingSuccess = false;
				$scope.uri.loading = false;
				$scope.uri.known = true;
				navStatus.endLoading();
			});
		}

		function loadSensorInfo(node) {
			navStatus.startLoading();
			$http({
				method: 'POST',
				url: constants.getApiUrl() + "/sensor/info/",
				data: node
			}).success(function (data) {
				$scope.sensorInfo = data;
				if ($scope.sensorInfo.adapterList && $scope.sensorInfo.adapterList.length > 0) {
					$scope.client.selectedAdapter = $scope.sensorInfo.adapterList[0];
				}
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
		}

		$scope.setMeasurementCount = function (count) {
			localStorageService.set("positioningmeasurmentCount", count);
			$scope.client.measurmentCount = count;
		};
		$scope.setDelay = function (delay) {
			localStorageService.set("positioningDelay", delay);
			$scope.client.delay = delay;
		};
		$scope.updateMulti = function () {
			localStorageService.set("positioningMulti", $scope.client.multi);
		};

		$scope.clearActualPosition = function() {
			localStorageService.remove("positioningActualPos");
		};

		function setActualPos (actualPos) {
			localStorageService.set("positioningActualPos", JSON.stringify(actualPos));
		}
		function getActualPos () {
			var json = localStorageService.get("positioningActualPos");
			if(json) {
				return json;
			}
			return undefined;
		}

		$scope.switchShouldIncludeExtendedNodes = function() {
			localStorageService.set("positioningShouldIncludeExtendedNodes", $scope.client.shouldIncludeExtendedNodes);
		};

		$scope.startPositioning = function () {
			navStatus.startLoading();
			$scope.mainLoader.errorMsg = undefined;
			$scope.mainLoader.description = 'Measuring Signal Strengths';
			$scope.mainLoader.progress = 0;
			$scope.uri.positioningLoading = true;
			$scope.measurement = undefined;
			$scope.probablePositionsWrapper = {};
			$scope.client.positionDuration = new Date().getTime();

			var node = createNodeFromInput();
			$http({
				method: 'GET',
				url: constants.getApiUrl() + "/positioning/survey/",
				params: {
					adapterName: $scope.client.selectedAdapter.name,
					ip: node.ip,
					port: node.port,
					htttps: false,
					count: $scope.client.measurmentCount,
					delayMs: $scope.client.delay
				}
			}).success(function (data) {
				$scope.measurement = data;
				$scope.uri.positioningLoading = false;
				navStatus.endLoading();
				findBestNetworks();
			}).error(function (data, status, headers, config) {
				$scope.uri.positioningLoading = false;
				$scope.mainLoader = {};
				navStatus.endLoading();
				if (status == 503) {
					$scope.mainLoader.errorMsg = "Could not complete RSS Survey"
				} else {
					navStatus.addError(status, data.statusDescription, data.exceptionList);
				}
			});
		};

		function findBestNetworks() {
			navStatus.startLoading();
			$scope.mainLoader.errorMsg = undefined;
			$scope.mainLoader.description = 'Finding network';
			$scope.mainLoader.progress = 60;
			$scope.uri.positioningLoading = true;
			$http({
				method: 'POST',
				url: constants.getApiUrl() + "/positioning/networks/",
				params: {freq: $scope.client.selectedAdapter.frequencyRange},
				data: $scope.measurement
			}).success(function (data) {
				$scope.fittingNetworks = data;
				$scope.uri.positioningLoading = false;
				navStatus.endLoading();
				if ($scope.fittingNetworks.length == 1) {
					$scope.selectedNetwork = $scope.fittingNetworks[0];
					findPosition();
				} else {
					$scope.mainLoader = {};
				}
			}).error(function (data, status, headers, config) {
				$scope.mainLoader = {};
				$scope.uri.positioningLoading = false;
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
		}

		function findPosition() {
			navStatus.startLoading();
			$scope.mainLoader.errorMsg = undefined;
			$scope.mainLoader.description = 'Calculating Position in ' + $scope.selectedNetwork.networkName;
			$scope.mainLoader.progress = 80;
			$http({
				method: 'POST',
				url: constants.getApiUrl() + "/positioning/position/",
				params: {
					freq: $scope.client.selectedAdapter.frequencyRange,
					networkId: $scope.selectedNetwork.networkId,
					multi: $scope.client.multi,
					shouldIncludeExtended: $scope.client.shouldIncludeExtendedNodes
				},
				data: $scope.measurement
			}).success(function (data) {
				$scope.probablePositionsWrapper = data;
				$scope.mainLoader = {};
				navStatus.endLoading();
				refreshCanvas(true);
				setFloorplanBlueprintUrl();
				$scope.client.positionDuration = new Date().getTime() - $scope.client.positionDuration;
			}).error(function (data, status, headers, config) {
				$scope.mainLoader = {};
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
		}

		$scope.setSelectedNetwork = function (network) {
			$scope.selectedNetwork = network;
			findPosition();
		};

		$scope.adapterLabel = function (adapter) {
			if (adapter) {
				return adapter.name + " [" + adapter.mode + "] (" + adapter.frequencyRange + ")";
			}
			return 'empty';
		};

		function refreshCanvas(redraw) {
			console.log("refresh canvas");
			setCallbacks();
			$scope.mapConf = {};
			if ($scope.mapConf.canvasConf == undefined) $scope.mapConf.canvasConf = {};
			if (redraw) {
				signalmapApi.redrawCanvas()
			}
			$scope.mapConf.pixelPerCm = 1;
		}

		function setCallbacks() {
			signalmapCallbacks.setOnCreateCallback(function (x, y) {
				console.log("oncreate signalmap in positiong.js");
				if ($scope.probablePositionsWrapper.probablePositions) {
					signalmapApi.switchShowGrid();
					signalmapApi.setPositioningMode(true);
					for (var i = 0; i < $scope.probablePositionsWrapper.probablePositions.bestPositions.length; i++) {
						signalmapApi.highlightTile($scope.probablePositionsWrapper.probablePositions.bestPositions[i].x, $scope.probablePositionsWrapper.probablePositions.bestPositions[i].y, 'best');
					}
					for (var i = 0; i < $scope.probablePositionsWrapper.probablePositions.goodPositions.length; i++) {
						signalmapApi.highlightTile($scope.probablePositionsWrapper.probablePositions.goodPositions[i].x, $scope.probablePositionsWrapper.probablePositions.goodPositions[i].y, 'good');
					}
					var actualPos = getActualPos();
					if (actualPos != undefined) {
						signalmapApi.highlightTile(actualPos.xTile, actualPos.yTile, 'actual');
					}
					if (!$scope.client.shouldIncludeExtendedNodes) {
						signalmapApi.switchShowExtendedNodes();
					}
				}

			});
			signalmapCallbacks.setGridMoveCallback(function (x, y) {
				$scope.mapConf.mapPosx = x;
				$scope.mapConf.mapPosy = y;
				$scope.$digest();
			});
			signalmapCallbacks.setActualPosCallback(function (actualPos) {
				setActualPos(actualPos);
				$scope.mapConf.actualPos = actualPos;
				$timeout(function(){
					$scope.$digest();
				});
			});
		}

		function setFloorplanBlueprintUrl() {
			if ($scope.serverConfig.couchDBUrl && $scope.probablePositionsWrapper.network) {
				for (var attachmentName in $scope.probablePositionsWrapper.network._attachments) {
					if (attachmentName.indexOf("blueprint") > -1) {
						$scope.probablePositionsWrapper.getFloorplanBlueprintSrc = $scope.serverConfig.couchDBUrl + "/" + $scope.probablePositionsWrapper.network._id + "/" + attachmentName + "?cache-control-rev=" + $scope.probablePositionsWrapper.network._attachments[attachmentName].revpos;
					}
				}
			}
		}
	}]);
})();