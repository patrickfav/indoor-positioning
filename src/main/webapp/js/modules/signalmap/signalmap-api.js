(function() {
    'use strict';

    angular.module('signalmap-ng').factory('signalmapApi',['signalmapState','signalmapUtils','signalmapUpdateAndTransform','signalmapInit', function(state,utils,update,init) {
        var api = {};

        api.switchDragModeGraph = function () {
            state.setDragModeGraph(!state.getDragModeGraph());
        };

        api.switchDragModeFloorplan = function () {
            state.setDragModeFloorplan(!state.getDragModeFloorplan());
        };

        api.flipHorizontal = function () {
            update.flip(state.getVertices(),"y",Math.max(state.getGridLengthX(),state.getGridLengthY())*state.getTileLenPx());
        };

        api.flipVertical = function () {
            update.flip(state.getVertices(),"x",Math.max(state.getGridLengthX(),state.getGridLengthY())*state.getTileLenPx());
        };

        api.rotateClockwise = function () {
            state.getGraphGroup().rotate(45);
			update.syncRotation();
        };

        api.rotateCounterclockwise = function () {
            state.getGraphGroup().rotate(-45);
            update.syncRotation();
        };

        api.rotateClockwiseFloorplan = function () {
            state.getFloorplanRaster().rotate(90);
            update.syncFloorplanAndUpdateView();
        };

        api.rotateCounterclockwiseFloorplan = function () {
            state.getFloorplanRaster().rotate(-90);
            update.syncFloorplanAndUpdateView();
        };

        api.switchShowGrid = function () {
            state.getGridGroup().visible = !state.getGridGroup().visible;
            update.updateView();
        };
        api.switchFloorplan = function () {
            if(state.getFloorplanRaster()!= undefined) {
                state.getFloorplanRaster().visible = !state.getFloorplanRaster().visible;
                update.updateView();
            }
        };

        api.switchShowEdges = function () {
            state.getEdgeGroup().visible = !state.getEdgeGroup().visible;
            update.updateView();
        };

		api.switchShowExtendedNodes = function () {
			update.switchShowExtendedNodes();
		};

        api.switchShowSignalStrengthCircles = function () {
            update.switchShowSignalStrengthCircles();
        };

        api.setFloorplanScale = function (scale) {
            update.floorplanScale(scale,state.getFloorplanRaster(),state.getFloorplanConfig());
        };

        api.redrawCanvas = function () {
            init.redrawCanvas(state.getLocalPaper(),state.getScope());
        };
        api.undoChangesGraph = function () {
	        if(state.getScope() && state.getScope().signalMap) {
		        init.undoChangesGraph(state.getScope().signalMap[state.getScope().freq]);
	        } else {
		        console.warn("signalmap has not been initialized yet, cannot undo");
	        }
        };

        api.exportCanvas = function(networkName,type) {
           utils.exportCanvas(networkName,type);
        };

		api.setLoadingState = function(isLoading) {
			if(state.isInitialized()) {
				state.getLoadingLayerGroup().visible = isLoading;
				update.updateView();
			}
		};

        api.highlightTile = function(x,y,type) {
            update.highlightTile(x,y,type);
        };

	    api.setPositioningMode = function(boolean) {
		    state.setPositioningMode(boolean);
	    };

        return api;
    }]);
})();