(function() {
	'use strict';

angular.module('adminConsoleApp').controller('NetworkDetailSignalMapCtrl', ['$scope','$http','navStatus','$window','Fullscreen','signalmapApi','signalmapCallbacks', function($scope, $http, navStatus,$window,Fullscreen,signalmapApi,signalmapCallbacks) {
	$scope.signalmapApi = signalmapApi;
	$scope.analysis = $scope.$parent.analysis;
	$scope.tabs = $scope.$parent.tabs;

	(function init() {
		console.log("init network details signalmap");
		refreshCanvas();
	})();

	$scope.$on('signalmap-fullreload', function(e) {
		console.log("reloading signalmap");
		$scope.analysis = $scope.$parent.analysis;
		refreshCanvas();
		signalmapApi.undoChangesGraph();
	});

	$scope.$on('signalmap-refreshCanvas', function(e) {
		console.log("refreshCanvas");
		$scope.analysis = $scope.$parent.analysis;
		refreshCanvas();
	});

	$scope.fullscreenMap = function () {
		if (Fullscreen.isEnabled()) {
			Fullscreen.cancel();
		} else {
			Fullscreen.enable($window.document.getElementById('signal-map-content'));
		}
	};


	$scope.resetSignalGraph = function() {
		if(confirm("Do you really want cancel all unsaved changes?")) {
			signalmapApi.undoChangesGraph();
			$scope.mapConf.showSignalStrengthCircles = false;
		}
	};
	$scope.switchDragMode = function() {
		$scope.mapConf.dragMode = !$scope.mapConf.dragMode;
		signalmapApi.switchDragModeGraph();
	};
	$scope.switchShowGrid = function() {
		$scope.mapConf.showMapGrid = !$scope.mapConf.showMapGrid;
		signalmapApi.switchShowGrid();
	};
	$scope.switchShowEdges= function() {
		$scope.mapConf.showEdges = !$scope.mapConf.showEdges;
		signalmapApi.switchShowEdges();
	};
	$scope.switchFloorplan = function() {
		$scope.mapConf.showFloorplan = !$scope.mapConf.showFloorplan;
		signalmapApi.switchFloorplan();
	};
	$scope.switchDragModeFloorplan = function() {
		$scope.mapConf.dragModeFloorplan = !$scope.mapConf.dragModeFloorplan;
		signalmapApi.switchDragModeFloorplan();
	};
	$scope.switchShowExtendedNodes = function() {
		$scope.mapConf.showExtendedNodes = !$scope.mapConf.showExtendedNodes;
		signalmapApi.switchShowExtendedNodes();
	};
	$scope.switchShowSignalStrengthCircles = function() {
		$scope.mapConf.showSignalStrengthCircles = !$scope.mapConf.showSignalStrengthCircles;
		signalmapApi.switchShowSignalStrengthCircles();
	};

	$scope.switchToolbarCollapse = function() {
		$scope.isToolbarCollapsed = !$scope.isToolbarCollapsed;
	};

	function refreshCanvas() {
		$scope.mapConf = {};
		if($scope.mapConf.canvasConf == undefined) $scope.mapConf.canvasConf = {};
		setCallbacks();
		signalmapApi.redrawCanvas();
		$scope.mapConf.mapFullscreenEnabled = Fullscreen.isEnabled;
		$scope.mapConf.showMapGrid = true;
		$scope.mapConf.showFloorplan = true;
		$scope.mapConf.showEdges = true;
		$scope.mapConf.showExtendedNodes = true;
		$scope.mapConf.showSignalStrengthCircles = false;
		$scope.mapConf.mapPosx = 0;
		$scope.mapConf.mapPosy = 0;
		$scope.mapConf.pixelPerCm = 1;

	}

	function setCallbacks() {
		signalmapCallbacks.setGridMoveCallback(function (x, y) {
			$scope.mapConf.mapPosx = x;
			$scope.mapConf.mapPosy = y;
			$scope.$digest();
		});
		signalmapCallbacks.setViewSizeChangedCallback(function (size) {
			$scope.mapConf.canvasConf.viewSize = size;
		});
		signalmapCallbacks.setFloorplanScaleChangedCallback(function (scale, freq) {
			$scope.analysis.signalMap[freq].floorplanConfig.scale = scale;
		});
		signalmapCallbacks.setFloorplanSizeChangedCallback(function (size) {
			$scope.mapConf.canvasConf.floorplanSize = size;
		});
	}

	$scope.removeExtendedNode = function(mac,freq) {
		for(var macKey in $scope.analysis.signalMap[freq].extendedNodes) {
			if(macKey === mac) {
				delete $scope.analysis.signalMap[freq].extendedNodes[macKey];
			}
		}
		signalmapApi.redrawCanvas();
	};

	$scope.setSpread = function(freq,spread) {
		$scope.analysis.signalMap[freq].spreadCmExtendedNodes = spread;
	};

	$scope.calcExtendedNodes = function(freq) {
		$scope.mapConf.calcExtendedNodes = true;
		signalmapApi.setLoadingState(true);
		navStatus.startLoading();
		$http({
			method: 'POST',
			params: {'freq':freq,'spreadCm':$scope.analysis.signalMap[freq].spreadCmExtendedNodes},
			url: constants.getApiUrl() + "/analysis/" + $scope.analysis.analysisId + "/calc-extended",
			data: $scope.analysis.signalMap[freq]
		}).success(function (data, status, headers) {
			$scope.analysis.signalMap[freq] = data;
			refreshCanvas();
			navStatus.endLoading();
			$scope.mapConf.calcExtendedNodes = false;
			signalmapApi.setLoadingState(false);
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status,data.statusDescription,data.exceptionList);
			$scope.mapConf.calcExtendedNodes = false;
			signalmapApi.setLoadingState(false);
		});
	};

	$scope.recalculateManagedNodes = function(freq) {
		navStatus.startLoading();
		$scope.mapConf.recalculateManagedNodesBtnLoading = true;
		signalmapApi.setLoadingState(true);
		$http({
			method: 'POST',
			params: {'freq':freq},
			url: constants.getApiUrl() + "/analysis/" + $scope.analysis.analysisId + "/recalc-signalmap"
		}).success(function (data, status, headers) {
			$scope.analysis = $scope.$parent.analysis = data;
			refreshCanvas();
			navStatus.endLoading();
			$scope.mapConf.recalculateManagedNodesBtnLoading = false;
			signalmapApi.setLoadingState(false);
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status,data.statusDescription,data.exceptionList);
			$scope.mapConf.recalculateManagedNodesBtnLoading = false;
			signalmapApi.setLoadingState(false);
		});
	};

	$scope.saveSignalMap = function(freq) {
		$scope.mapConf.saveSignalMap = true;
		signalmapApi.setLoadingState(true);
		navStatus.startLoading();
		$http({
			method: 'POST',
			params: {'freq':freq},
			url: constants.getApiUrl() + "/analysis/" + $scope.analysis.analysisId + "/save-signal-map",
			data: $scope.analysis.signalMap[freq]
		}).success(function (data, status, headers) {
			$scope.analysis = $scope.$parent.analysis = data;
			refreshCanvas();
			navStatus.endLoading();
			$scope.mapConf.saveSignalMap = false;
			signalmapApi.setLoadingState(false);
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status,data.statusDescription,data.exceptionList);
			$scope.mapConf.saveSignalMap = false;
			signalmapApi.setLoadingState(false);
		});
	};
}]);

})();