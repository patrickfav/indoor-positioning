(function() {
	'use strict';

	angular.module('signalmap-ng').factory('signalmapState', function() {
		var state = {};

        var paper;
        var scope;
		var tileLenPx;
		var gridLengthX;
		var gridLengthY;
		var floorplanConfig;

		var vertices = {};

		var graphGroup;
		var edgeGroup;
		var gridGroup;
		var loadingLayerGroup;

		var floorplanRaster;

		var dragModeGraph = false;
		var dragModeFloorplan = false;
		var positioningMode = false;

		var actualPosTile;
		var bestPosTile;

		var initialized = false;

		state.constructor = function(paperParam, scopeParam, tileLenPxParam,gridLengthXParam,gridLengthYParam,floorplanConfigParam) {
            state.reset();
            scope = scopeParam;
            paper = paperParam;
			tileLenPx = tileLenPxParam;
			gridLengthX = gridLengthXParam;
			gridLengthY = gridLengthYParam;
			floorplanConfig = floorplanConfigParam;
			initialized=true;
		};

        state.reset = function() {
            dragModeGraph = false;
            dragModeFloorplan = false;
            vertices = {};
			scope=undefined;
			paper=undefined;
			initialized=false;

			if(graphGroup && graphGroup.removeChildren) { graphGroup.removeChildren();}
			if(edgeGroup && edgeGroup.removeChildren) { edgeGroup.removeChildren();}
			if(gridGroup && graphGroup.removeChildren) { gridGroup.removeChildren();}
			if(loadingLayerGroup && loadingLayerGroup.removeChildren) { loadingLayerGroup.removeChildren();}

			graphGroup=undefined;
			edgeGroup=undefined;
			gridGroup=undefined;
			loadingLayerGroup=undefined;

			if(floorplanRaster && floorplanRaster.remove) {floorplanRaster.remove()}
			floorplanRaster =undefined;

	        if(actualPosTile != undefined) {actualPosTile.remove();actualPosTile=undefined;}
	        if(bestPosTile != undefined) {bestPosTile.remove();bestPosTile=undefined;}
        };

		state.isInitialized = function() {
			return scope != undefined && paper != undefined;
		};

        state.getScope = function() {
            return scope;
        };

        state.getSignalMapFromScope = function() {
            return scope.signalMap[scope.freq];
        };

        state.getLocalPaper = function() {
            return paper;
        };

		state.getTileLenPx = function() {
			return tileLenPx;
		};
		state.setTileLenPx = function(length) {
			tileLenPx = length;
		};

		state.getGridLengthY = function() {
			return gridLengthY;
		};
		state.setGridLengthY = function(length) {
			gridLengthY = length;
		};

		state.getGridLengthX = function() {
			return gridLengthX;
		};
		state.setGridLengthX = function(length) {
			gridLengthX = length;
		};

		state.getFloorplanConfig = function() {
			return floorplanConfig;
		};
		state.setFloorplanConfig = function(config) {
			floorplanConfig = config;
		};

		state.getVertices = function() {
			return vertices;
		};

		state.getGraphGroup = function() {
            if(graphGroup == undefined) {graphGroup = new paper.Group();}
			return graphGroup;
		};
		state.getEdgeGroup = function() {
            if(edgeGroup == undefined) {edgeGroup = new paper.Group();}
            return edgeGroup;
		};
		state.getGridGroup = function() {
            if(gridGroup == undefined) {gridGroup = new paper.Group();}
            return gridGroup;
		};
		state.getLoadingLayerGroup = function() {
			if(loadingLayerGroup == undefined) {loadingLayerGroup = new paper.Group();}
			return loadingLayerGroup;
		};
		state.getFloorplanRaster = function() {
			return floorplanRaster;
		};
        state.setFloorplanRaster = function(raster) {
            floorplanRaster = raster;
        };
		state.getDragModeGraph = function() {
			return dragModeGraph;
		};
		state.setDragModeGraph = function(boolean) {
			dragModeGraph = boolean;
		};

		state.getDragModeFloorplan = function() {
			return dragModeFloorplan;
		};
		state.setDragModeFloorplan = function(boolean) {
			dragModeFloorplan = boolean;
		};
		state.getPositioningMode = function() {
			return positioningMode;
		};
		state.setPositioningMode = function(boolean) {
			positioningMode = boolean;
		};

		state.getActualPosTile = function() {
			return actualPosTile;
		};
		state.setActualPosTile = function(actualTile) {
			actualPosTile = actualTile;
		};

		state.getBestPosTile = function() {
			return bestPosTile;
		};
		state.setBestPosTile = function(bestTile) {
			bestPosTile = bestTile;
		};


		return state;
	});
})();