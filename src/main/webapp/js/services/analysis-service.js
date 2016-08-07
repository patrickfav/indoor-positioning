(function() {
	'use strict';

	angular.module('adminConsoleApp').factory('analysisService',['$http', function($http) {
		var service ={};

		var analysis;
		var analysisProvider;

		service.setAnalysisProvider = function(analysisProviderFunction) {
			analysisProvider = analysisProviderFunction;
		};
		service.getAnalysis = function() {
			if(analysisProvider) {
				return analysisProvider();
			}
			return undefined;
		};

		service.setAnalysisMultiplier = function(freq,multiplier,analysisId) {
			return $http({
				method: 'PUT',
				params: {'freq':freq,'multiplier':multiplier},
				url: constants.getApiUrl() + "/analysis/" + analysisId + "/multiplier"
			})
		};


		return service;
	}]);

})();