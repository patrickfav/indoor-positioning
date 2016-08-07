(function() {
	'use strict';

	angular.module('adminConsoleApp').controller('PathLossModalCtrl', ['$scope', '$http', 'navStatus','$modalInstance','analysisService','$rootScope','localStorageService',
		function ($scope, $http, navStatus,$modalInstance,analysisService,$rootScope,localStorageService) {
		$scope.pathLossData = {};
		$scope.network = angular.copy($scope.$parent.wrapper.network);
		$scope.analysis = angular.copy(analysisService.getAnalysis());
		$scope.distanceMatrix = {};
		$scope.distMatrixConfig = {};

		(function init () {
			fetchValuesForPathLossGraph();
			getDistanceValuesForInput();
		})();

		$scope.setEnvModel = function (envModel) {
			$scope.network.environmentModel = envModel;
			getDistanceValuesForInput();
		};

		$scope.saveNetwork = function () {
			navStatus.startLoading();
			$http({
				method: 'PUT',
				url: constants.getApiUrl() + "/network",
				data: $scope.network
			}).success(function () {
				navStatus.endLoading();
				$scope.$emit('updateSensorNodesAndNetwork');
				$modalInstance.close();
			}).error(function (data, status, headers, config) {
				navStatus.addError(status, data.statusDescription, data.exceptionList);
				navStatus.endLoading();
			});
			if($scope.distMatrixConfig && $scope.analysis) {
				for(var freq in $scope.distMatrixConfig) {
					navStatus.startLoading();
					analysisService.setAnalysisMultiplier(freq, $scope.distMatrixConfig[freq].multi, $scope.analysis.analysisId)
					.success(function (data, status, headers) {
						navStatus.endLoading();
						$scope.analysis._rev = data.updatedRev;
						analysisService.getAnalysis()._rev = data.updatedRev;
						$rootScope.$broadcast('reloadCurrentAnalysis');
					}).error(function (data, status, headers, config) {
						navStatus.endLoading();
						navStatus.addError(status, data.statusDescription, data.exceptionList);
					});
				}
			}

			if($scope.distMatrixConfig) {
				navStatus.startLoading();
				$http({
					method: 'POST',
					url: constants.getApiUrl() + "/network/"+$scope.network.networkId+"/node/multiplier",
					data: getMultResultFreqMap()
				}).success(function () {
					navStatus.endLoading();
					$scope.$emit('updateSensorNodesAndNetwork');
					$modalInstance.close();
				}).error(function (data, status, headers, config) {
					navStatus.addError(status, data.statusDescription, data.exceptionList);
					navStatus.endLoading();
				});
			}
		};

		function getMultResultFreqMap() {
			var map = {};
			if($scope.distMatrixConfig) {
				for (var freq in $scope.distMatrixConfig) {
					if($scope.distMatrixConfig[freq].multResult) {
						map[freq] = $scope.distMatrixConfig[freq].multResult;
					}
				}
			}
			return map;
		}

		$scope.previewGrah = function() {
			if($scope.radioPathLossForm.$dirty) {
				fetchValuesForPathLossGraph();
				getDistanceValuesForInput();
			}
		};

		function fetchValuesForPathLossGraph() {
			navStatus.startLoading();
			$http({
				method: 'POST',
				url: constants.getApiUrl()+"/util/itu-degr-dist-values",
				data: $scope.network.pathLossConfig})
				.success(function (data, status, headers, config) {
					$scope.pathLossData.values = data;
					$scope.pathLossData.etag = headers("Etag");
					constructPathLossGraph();
					navStatus.endLoading();
				}).error(function (data, status) {
					navStatus.endLoading();
					navStatus.addError(status,data.statusDescription,data.exceptionList);
				});
		}

		function constructPathLossGraph() {
			$scope.pathLossData.chart = {
				title: {text: ''},
				subtitle: {text: ''},
				options: {
					chart: {type: 'line', zoomType: 'x',height:300},
					tooltip: {crosshairs: false},
					credits: {"enabled": false},
					plotOptions: {
						line: {
							lineWidth: 2,
							marker: {enabled: false},
							tooltip: {
								headerFormat: '<strong>{point.y:.2f}m</strong><br>',
								pointFormat: '<strong>{point.x:.0f}dB</strong>'
							}
						}
					}
				},
				xAxis: {
					title: {text: 'Path Loss (dB)'},
					labels: {formatter: function () {
						return this.value.toString() + "dB";
					}}
				},
				yAxis: {
					title: {text: 'Distance'},
					labels: {formatter: function () {
						return this.value.toString() + "m";}},
					min:0
				},
				loading: false,
				series: []
			};

			for(var model in $scope.pathLossData.values) {
				$scope.pathLossData.chart.series.push({
					id: model,
					name: model,
					visible: model == $scope.network.environmentModel,
					data: $scope.pathLossData.values[model]
				})
			}

		}

		function getDistanceValuesForInput() {
			if($scope.analysis) {
				$scope.pathLossData.valuesForInput = {};
				for(var freq in $scope.analysis.extendedNodeMap) {
					navStatus.startLoading();
					$http({
						method: 'POST',
						url: constants.getApiUrl() + "/util/itu-degr-dist-values-for-input",
						params:{envModel:$scope.network.environmentModel, freq: freq},
						data: {config:$scope.network.pathLossConfig,values:getInputList(freq)}})
						.success(function (data, status, headers, config) {
							$scope.pathLossData.valuesForInput[freq] = data;
							prepareDistanceMatrix(freq);
							navStatus.endLoading();
						}).error(function (data, status) {
							navStatus.endLoading();
							navStatus.addError(status, data.statusDescription, data.exceptionList);
						});
				}
			}
		}

		function getInputList(freq) {
			var valueList =[];
			for(var i=0;i<$scope.analysis.extendedNodeMap[freq].length;i++) {
				for (var j = 0; j < $scope.analysis.extendedNodeMap[freq][i].managedNodes.length; j++) {
					if ($scope.analysis.extendedNodeMap[freq][i].managedNode) {
						valueList.push(Math.abs($scope.analysis.extendedNodeMap[freq][i].managedNodes[j].statistics.mean)
							* $scope.analysis.extendedNodeMap[freq][i].managedNodes[j].physicalAdapter.multiplier);
					}
				}
			}
			return valueList;
		}

		function prepareDistanceMatrix(freq) {
			if($scope.distMatrixConfig[freq] == undefined) {
				$scope.distMatrixConfig[freq] = {};
				$scope.distMatrixConfig[freq].viewMode = 'Distance';
			}
			if($scope.distMatrixConfig[freq].multi == undefined) {
				$scope.distMatrixConfig[freq].multi = $scope.analysis.distMultiMap[freq];
			}

			if($scope.distanceMatrix[freq] == undefined) {
				$scope.distanceMatrix[freq] ={};
			}

			for(var i=0;i<$scope.analysis.extendedNodeMap[freq].length;i++) {
				for(var j=0;j<$scope.analysis.extendedNodeMap[freq][i].managedNodes.length;j++) {
					if($scope.analysis.extendedNodeMap[freq][i].managedNode) {
						var managedNode = $scope.analysis.extendedNodeMap[freq][i].managedNodes[j];
						var macSource = managedNode.physicalAdapter.macAddress;
						var macTarget = $scope.analysis.extendedNodeMap[freq][i].macAddress;

						if($scope.distanceMatrix[freq][macSource] == undefined) {
							$scope.distanceMatrix[freq][macSource] = {};
						}

						if($scope.distanceMatrix[freq][macSource][macTarget] == undefined) {
							$scope.distanceMatrix[freq][macSource][macTarget] = {};
						}

						$scope.distanceMatrix[freq][macSource][macTarget].dbmMean = managedNode.statistics.mean*managedNode.physicalAdapter.multiplier;
						$scope.distanceMatrix[freq][macSource][macTarget].dbmMeanNonMultiplied = managedNode.statistics.mean;
						$scope.distanceMatrix[freq][macSource][macTarget].dbmMedian = managedNode.statistics.median*managedNode.physicalAdapter.multiplier;
						if($scope.pathLossData.valuesForInput && $scope.pathLossData.valuesForInput[freq]) {
							$scope.distanceMatrix[freq][macSource][macTarget].distance = ($scope.pathLossData.valuesForInput[freq][Math.abs(managedNode.statistics.mean*managedNode.physicalAdapter.multiplier)]*$scope.distMatrixConfig[freq].multi);
						} else {
							$scope.distanceMatrix[freq][macSource][macTarget].distance = (managedNode.radioModelData.distanceMap.ITUIndoorModelDegradingDist.multMeanDistance*$scope.distMatrixConfig[freq].multi);
						}
						$scope.distanceMatrix[freq][macSource][macTarget].editMode = false;

						if(localStorageService.get(macSource+macTarget) != null) {
							$scope.distanceMatrix[freq][macSource][macTarget].targetDistance = localStorageService.get(macSource+macTarget);
						}
					}
				}
			}
			updateOffset(freq);
		}

		$scope.setDistMatrixViewMode = function(freq,mode) {
			if($scope.distMatrixConfig[freq]) {
				$scope.distMatrixConfig[freq].viewMode = mode;
			}
		};

		function updateOffset(freq) {
			if($scope.distMatrixConfig[freq]) {
				$scope.distMatrixConfig[freq].offset = 0;
				$scope.distMatrixConfig[freq].offsetdBm=0;
				for (var i = 0; i < $scope.analysis.extendedNodeMap[freq].length; i++) {
					for (var j = 0; j < $scope.analysis.extendedNodeMap[freq][i].managedNodes.length; j++) {
						if ($scope.analysis.extendedNodeMap[freq][i].managedNode) {
							var macSrc = $scope.analysis.extendedNodeMap[freq][i].managedNodes[j].physicalAdapter.macAddress;
							var macTarget = $scope.analysis.extendedNodeMap[freq][i].macAddress;
							if ($scope.distanceMatrix[freq][macSrc][macTarget].targetDistance != undefined && $scope.distanceMatrix[freq][macSrc][macTarget].targetDistance > 0) {
								$scope.distMatrixConfig[freq].offset += Math.abs($scope.distanceMatrix[freq][macSrc][macTarget].distance - $scope.distanceMatrix[freq][macSrc][macTarget].targetDistance);
							}
							$scope.distMatrixConfig[freq].offsetdBm += Math.abs(Math.abs($scope.distanceMatrix[freq][macSrc][macTarget].dbmMean) - Math.abs($scope.distanceMatrix[freq][macTarget][macSrc].dbmMean));
						}
					}
				}
			}
		}

		$scope.setAnalysisMultiplier = function(freq) {
			prepareDistanceMatrix(freq);
		};

		$scope.switchEditMode = function (freq,macSource,macTarget,show) {
			if($scope.distanceMatrix[freq] &&  $scope.distanceMatrix[freq][macSource] && $scope.distanceMatrix[freq][macSource][macTarget]) {
				$scope.distanceMatrix[freq][macSource][macTarget].editMode = show;
				if($scope.distanceMatrix[freq] &&  $scope.distanceMatrix[freq][macTarget] && $scope.distanceMatrix[freq] &&  $scope.distanceMatrix[freq][macTarget][macSource]) {
					$scope.distanceMatrix[freq][macTarget][macSource].targetDistance = $scope.distanceMatrix[freq][macSource][macTarget].targetDistance;
					localStorageService.set(macTarget+macSource,$scope.distanceMatrix[freq][macSource][macTarget].targetDistance);
					localStorageService.set(macSource+macTarget,$scope.distanceMatrix[freq][macSource][macTarget].targetDistance);
				}
				updateOffset(freq);
			}
		};

		$scope.clearTargetDistanceStorage = function(freq) {
			if($scope.distanceMatrix[freq]) {
				for(var macSource in $scope.distanceMatrix[freq]) {
					for(var macTarget in $scope.distanceMatrix[freq][macSource]) {
						$scope.distanceMatrix[freq][macTarget][macSource].targetDistance = undefined;
						localStorageService.remove(macTarget+macSource);
					}
				}
			}
			prepareDistanceMatrix(freq);
		};

		$scope.getBestConfigByBruteforce = function(freq) {
			$scope.calcErrorMsg = '';
			if($scope.analysis) {
				var data = getDataForBruteforceSolver(freq);
				if(data.length > 0) {
					navStatus.startLoading();
					$scope.calcBtnLoading = true;
					$http({
						method: 'POST',
						url: constants.getApiUrl() + "/util/itu-degr-dist-bruteforce-solver",
						params: {envModel: $scope.network.environmentModel, freq: freq},
						data: data})
						.success(function (data, status, headers, config) {
							$scope.network.pathLossConfig = data.config;
							$scope.distMatrixConfig[freq].multi = data.mult;
							fetchValuesForPathLossGraph();
							getDistanceValuesForInput();
							$scope.calcBtnLoading = false;
							navStatus.endLoading();
						}).error(function (data, status) {
							navStatus.endLoading();
							$scope.calcBtnLoading = false;
							navStatus.addError(status, data.statusDescription, data.exceptionList);
						});
				} else {
					$scope.calcErrorMsg = "No target distances set"
				}
			}
		};

		function getDataForBruteforceSolver(freq) {
			var data = [];
			if($scope.distanceMatrix[freq]) {
				for(var macSrc in $scope.distanceMatrix[freq]) {
					for(var macTarget in $scope.distanceMatrix[freq][macSrc]) {
						if($scope.distanceMatrix[freq][macSrc][macTarget].targetDistance != undefined && $scope.distanceMatrix[freq][macSrc][macTarget].targetDistance > 0) {
							data.push({
								dBmMean: $scope.distanceMatrix[freq][macSrc][macTarget].dbmMean,
								targetDistanceMeter: $scope.distanceMatrix[freq][macSrc][macTarget].targetDistance
							})
						}
					}
				}
			}
			return data;
		}

		$scope.calibrateNodeMultiplier = function(freq) {
			$scope.calcErrorMsg = '';
			if($scope.analysis) {
				var data = getDataForMultCalibration(freq);
				navStatus.startLoading();
				$scope.calibrateBtnLoading = true;
				$http({
					method: 'POST',
					url: constants.getApiUrl() + "/util/itu-degr-dist-bruteforce-calibrate-mult",
					params: {freq: freq},
					data: data})
					.success(function (data, status, headers, config) {
						$scope.calibrateBtnLoading = false;
						$scope.distMatrixConfig[freq].multResult = data;
						setNewNodeMultiplier(freq,data);
						getDistanceValuesForInput();
						navStatus.endLoading();
					}).error(function (data, status) {
						navStatus.endLoading();
						$scope.calibrateBtnLoading = false;
						navStatus.addError(status, data.statusDescription, data.exceptionList);
					});

			}
		};

		function getDataForMultCalibration(freq) {
			var data = [];

			if($scope.distanceMatrix[freq]) {
				for(var macSrc in $scope.distanceMatrix[freq]) {
					for(var macTarget in $scope.distanceMatrix[freq][macSrc]) {
						data.push({
							dBm1:$scope.distanceMatrix[freq][macSrc][macTarget].dbmMeanNonMultiplied,
							dBm2:$scope.distanceMatrix[freq][macTarget][macSrc].dbmMeanNonMultiplied,
							mac1:macSrc,
							mac2:macTarget
						});
					}
				}
			}

			//remove duplicates
			for(var i= 0;i<data.length;i++) {
				for(var j= 0;j<data.length;j++) {
					if(data[i].mac1 == data[j].mac2 && data[i].mac2 == data[j].mac1) {
						data.splice(j,1);
					}
				}
			}
			return data;
		}

		function setNewNodeMultiplier(freq,bruteforceMultResult) {
			for(var i=0;i<$scope.analysis.extendedNodeMap[freq].length;i++) {
				for (var j = 0; j < $scope.analysis.extendedNodeMap[freq][i].managedNodes.length; j++) {
					if ($scope.analysis.extendedNodeMap[freq][i].managedNode) {
						var managedNode = $scope.analysis.extendedNodeMap[freq][i].managedNodes[j];

						if(bruteforceMultResult.macMultMap[managedNode.physicalAdapter.macAddress]) {
							managedNode.physicalAdapter.multiplier = bruteforceMultResult.macMultMap[managedNode.physicalAdapter.macAddress];
						}
					}
				}
			}

			for(var j=0;j<$scope.analysis.physicalAdaptersMap[freq].length;j++){
				var phyAdapter = $scope.analysis.physicalAdaptersMap[freq][j];
				if(bruteforceMultResult.macMultMap[phyAdapter.macAddress]) {
					phyAdapter.multiplier = bruteforceMultResult.macMultMap[phyAdapter.macAddress];
				}
			}

		}

		$scope.close = function () {
			$scope.$emit('updateSensorNodesAndNetwork');
			$modalInstance.close();
		};

	}]);
})();
