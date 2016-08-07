(function() {
	'use strict';

angular.module('adminConsoleApp').controller('NetworkDetailAnalysisCtrl', ['$scope','$http','navStatus','$window','$routeParams','analysisService', function($scope, $http, navStatus,$window,$routeParams,analysisService) {
	var analysisPerPage = 5;

	$scope.networkId = "";
	$scope.distanceMatrix = {};
	$scope.analysis={};
	$scope.analysisMetaList={};
	$scope.sigmaGraph={};
	$scope.tabs = {};

	(function init() {
		console.log("init network details anaylsis");
		$scope.networkId = $routeParams.networkId;
		reloadAnalysisMetaList();
	})();

	function reloadAnalysisMetaList() {
		if($scope.analysisMetaList.keyElem == undefined) {$scope.analysisMetaList.keyElem = {};}
		if($scope.analysisMetaList.prevElemStack == undefined) { $scope.analysisMetaList.prevElemStack=[];}

		navStatus.startLoading();
		$http({
			method: 'GET',
			params: {'start-key-date':$scope.analysisMetaList.keyElem.created,networkId:$scope.networkId,'start-doc-id':$scope.analysisMetaList.keyElem._id,limit:analysisPerPage},
			url: constants.getApiUrl() +"/analysis/",
			headers: {'If-None-Match': $scope.analysisMetaList.etag}})
			.success(function (data, status, headers, config) {
				if($scope.analysisMetaList.list && $scope.analysisMetaList.actionNext) {
					$scope.analysisMetaList.prevElemStack.push($scope.analysisMetaList.list[0]);
				}

				$scope.analysisMetaList.hasNext = data.length >= analysisPerPage+1;
				$scope.analysisMetaList.hasPrev = $scope.analysisMetaList.prevElemStack.length > 0;
				$scope.analysisMetaList.nextElem = data[analysisPerPage];
				$scope.analysisMetaList.list = data.splice(0,analysisPerPage);

				$scope.analysisMetaList.etag = headers("Etag");
				if($scope.analysisMetaList.list && $scope.analysisMetaList.list.length > 0 && !$scope.analysis.etag) {
					$scope.loadAnalysis($scope.analysisMetaList.list[0].analysisId);
				}
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status,data.statusDescription,data.exceptionList);
			});
	}

	$scope.$on('reloadAnalysis', function(e) {
		console.log("reloading analysis");
		reloadAnalysisMetaList();
		$scope.$broadcast('signalmap-fullreload');
	});

	$scope.$on('reloadCurrentAnalysis', function(e) {
		if($scope.analysis.analysisId) {
			console.log("reloading current analysis");
			$scope.loadAnalysis($scope.analysis.analysisId);
		}
	});

	$scope.loadAnalysis = function(analysisId) {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/analysis/"+analysisId,
			headers: {'If-None-Match': $scope.analysis.etag}})
			.success(function (data, status, headers, config) {
				$scope.analysis = data;
				$scope.analysis.etag = headers("Etag");
				$scope.$broadcast('signalmap-refreshCanvas');
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status,data.statusDescription,data.exceptionList);
			});
	};

	$scope.deleteAnalysis = function (analysisId) {
		if(confirm("Do you really want to delete this Analysis?")) {
			navStatus.startLoading();
			$http({
				method: 'DELETE',
				url: constants.getApiUrl() + "/analysis/" + analysisId
			}).success(function () {
				navStatus.endLoading();
				$scope.analysis={};
				$scope.analysisMetaList={};
				reloadAnalysisMetaList();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status,data.statusDescription,data.exceptionList);
			});
		}
	};

	/* ******************************************************************** EXTENDED NODE LIST FEATURES */


	$scope.setRoomForExtNode =function(macAddress,roomId) {
		navStatus.startLoading();
		$http({
			method: 'PUT',
			params: {'roomId':roomId,'macAddress':macAddress},
			url: constants.getApiUrl() +"/networkservice/" + $scope.networkId + "/roomlist/mapping"
		}).success(function (data, status, headers) {
			navStatus.endLoading();
			$scope.$emit('updateSensorNodesAndNetwork');
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status,data.statusDescription,data.exceptionList);
		});
	};

	$scope.getRoomByMapping = function(macAddress) {
		if($scope.wrapper.roomList && macAddress) {
			for(var macKey in $scope.wrapper.roomList.macToRoomIdMap) {
				if(macKey == macAddress) {
					return $scope.getRoomNameById($scope.wrapper.roomList.macToRoomIdMap[macKey]);
				}
			}
		}
		return "unknown"
	};

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

	/* ******************************************************************** ANALYSIS PAGINATION */


	$scope.nextAnalysis = function() {
		if($scope.analysisMetaList.hasNext) {
			$scope.analysisMetaList.etag = '';
			$scope.analysisMetaList.keyElem = $scope.analysisMetaList.nextElem;
			$scope.analysisMetaList.actionNext = true;
			reloadAnalysisMetaList();
		}
	};
	$scope.prevAnalysis = function() {
		if($scope.analysisMetaList.hasPrev) {
			$scope.analysisMetaList.etag = '';
			$scope.analysisMetaList.keyElem = $scope.analysisMetaList.prevElemStack.pop();
			$scope.analysisMetaList.actionNext = false;
			reloadAnalysisMetaList();
		}
	};

	/* ******************************************************************** DISTANCE MATRIX CELLS */


	$scope.getDistanceMatrixValue = function(freq,macTarget,macSource) {
		var managedNode = $scope.getDistanceMatrixStat(freq,macTarget,macSource);

		if(managedNode != undefined) {
			var parts = $scope.distanceMatrix[freq].value.split(".");
			var obj = managedNode;
			for(var i=0;i<parts.length;i++) {
				obj = obj[parts[i]];
			}

			if($scope.distanceMatrix[freq].unit=='m') {
				obj *= $scope.analysis.distMultiMap[freq];
			}

			if($scope.distanceMatrix[freq].useMult && $scope.distanceMatrix[freq].unit == 'dBm') {
				obj *= managedNode.physicalAdapter.multiplier;
			}

			return obj;
		}
	};

	$scope.getDistanceMatrixStat = function (freq,macTarget,macSource) {
		for(var i=0;i<$scope.analysis.extendedNodeMap[freq].length;i++) {
			for(var j=0;j<$scope.analysis.extendedNodeMap[freq][i].managedNodes.length;j++) {
				if($scope.analysis.extendedNodeMap[freq][i].macAddress == macTarget &&
					$scope.analysis.extendedNodeMap[freq][i].managedNodes[j].physicalAdapter.macAddress == macSource) {

					if($scope.distanceMatrix[freq] == undefined) {
						$scope.distanceMatrix[freq]={};
						$scope.distanceMatrix[freq].value = 'statistics.mean';
						$scope.distanceMatrix[freq].unit = 'dBm';
						$scope.distanceMatrix[freq].label = 'Mean';
						$scope.distanceMatrix[freq].useMult = true;
						$scope.distanceMatrix[freq].showOnlyManaged = false;
					}

					return $scope.analysis.extendedNodeMap[freq][i].managedNodes[j];
				}
			}
		}
	};

	$scope.getDistanceMatrixTooltip = function(freq,macTarget,macSource) {
		var managedNode = $scope.getDistanceMatrixStat(freq,macTarget,macSource);
		if(managedNode != undefined) {
			return managedNode.statistics.mean.toFixed(2) + "dBm +/-"
				+ managedNode.statistics.sdtErr80Interval.toFixed(2)
				+ " (" + managedNode.statistics.dataSize + ") <br/>"+managedNode.radioModelData.distanceMap.ITUIndoorModelDegradingDist.meanDistance.toFixed(2)+"m ("+
				managedNode.radioModelData.distanceMap.ITUIndoorModelDegradingDist.roomsBetween+" rooms dist.)";
		}
	};

	$scope.setDistanceMatrixValue = function(freq,valueType,label) {
		$scope.distanceMatrix[freq].value = valueType;
		$scope.distanceMatrix[freq].label = label;

		if(valueType.indexOf("statistics.")> -1) {
			$scope.distanceMatrix[freq].unit  = 'dBm';
		} else {
			$scope.distanceMatrix[freq].unit  = 'm';
		}
	};

	$scope.hoverCell = function(freq,mac1,mac2) {
		if(mac1 != mac2) {
			var elem = angular.element($window.document.getElementById(mac2 + "_" + mac1));
			if(elem) {
				elem.addClass("cross-highlight");
				angular.element($window.document.getElementById(mac1 + "_" + mac2)).addClass("cross-highlight");
			}
		}
		$scope.analysis.selectedNode = $scope.getDistanceMatrixStat(freq,mac2,mac1);
	};
	$scope.leaveCell = function(mac1,mac2) {
		if(mac1 != mac2) {
			var elem = angular.element($window.document.getElementById(mac2 + "_" + mac1));
			if(elem) {
				elem.removeClass("cross-highlight");
				angular.element($window.document.getElementById(mac1+ "_" + mac2)).removeClass("cross-highlight");
			}
		}
		$scope.analysis.selectedNode = undefined;
	};

	/* ******************************************************************** DISTANCE MATRIX FEATURES */

	$scope.setAnalysisMultiplier = function(freq,multiplier,analysisId) {
		navStatus.startLoading();
		analysisService.setAnalysisMultiplier(freq,multiplier,analysisId)
		.success(function (data, status, headers) {
			navStatus.endLoading();
			$scope.analysis._rev = data.updatedRev;
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status,data.statusDescription,data.exceptionList);
		});
	};

	$scope.switchShowOnlyManaged = function(freq) {
		$scope.distanceMatrix[freq].showOnlyManaged = !$scope.distanceMatrix[freq].showOnlyManaged;
	};

	$scope.switchDistanceMatrixMult = function(freq) {
		$scope.distanceMatrix[freq].useMult = !$scope.distanceMatrix[freq].useMult;
	};

	/* ******************************************************************** MISC */

	$scope.tabSelected = function(type,selected) {
		$scope.tabs[type] = selected;
	};

	analysisService.setAnalysisProvider(function() {
		return $scope.analysis;
	})

}]);

})();