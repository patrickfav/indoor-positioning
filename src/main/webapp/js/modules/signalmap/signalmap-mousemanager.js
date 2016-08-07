(function() {
	'use strict';

	angular.module('signalmap-ng').factory('signalmapMousemanager',['signalmapState','signalmapUtils','signalmapCallbacks','signalmapUpdateAndTransform', function(state,utils,callbacks,update) {
		var mouseManager = {};

		var currentMouseTilePos = {};
		var lastMouseEventCallback;
		var drag = false;

		mouseManager.onMouseDown = function() {
			drag = true;
			if (state.getDragModeGraph()) {
				var boundsRec = new paper.Path.Rectangle(state.getGraphGroup().bounds);
				boundsRec.strokeColor = 'pink';
				boundsRec.strokeWidth = 3;
				boundsRec.dashArray = [5, 4];
				boundsRec.name = "bounds";
				state.getGraphGroup().opacity = 0.5;
				state.getGraphGroup().addChild(boundsRec);
			}

			if (state.getDragModeFloorplan()) {
				state.getFloorplanRaster().opacity = 0.5;
			}
		};

		mouseManager.onMouseUp = function () {
			drag = false;
			if (state.getDragModeGraph()) {
				state.getGraphGroup().children["bounds"].remove();
				state.getGraphGroup().opacity = 1;
				state.getGraphGroup().bringToFront();
				state.getGraphGroup().position = utils.getSnapPosition(state.getGraphGroup().position,state.getTileLenPx());
			}

			if (state.getDragModeFloorplan()) {
				state.getFloorplanRaster().opacity = 1;
				update.syncFloorplanAndUpdateView();
			}
		};

		mouseManager.onMouseMove =function(event) {
			broadcastMousePos(event);

			if (drag && state.getDragModeGraph()) {
				setBrowserSpecificPosFromMouseEventToItem(state.getGraphGroup(),event);
                update.syncAllNodesAndUpdate();
			}

			if(drag && state.getDragModeFloorplan()) {
				setBrowserSpecificPosFromMouseEventToItem(state.getFloorplanRaster(),event);
				update.updateView();
			}
		};

		mouseManager.onClick = function(event) {
			if(event && state.getPositioningMode()) {
				var pos = getBrowserSpecificPosFromMouseEvent(event);
				var actualPos={};
				actualPos.xTile = Math.round(pos.x/state.getTileLenPx());
				actualPos.yTile = Math.round(pos.y/state.getTileLenPx());
				update.highlightTile(actualPos.xTile,actualPos.yTile,'actual');
			}
		};

		function broadcastMousePos(point) {
			if(!lastMouseEventCallback || lastMouseEventCallback < (new Date().getTime())-30) { //performance optimization only update if at least 30ms have passed
				var x = Math.floor(point.layerX / state.getTileLenPx());
				var y = Math.floor(point.layerY / state.getTileLenPx());
				if (currentMouseTilePos.x != x || currentMouseTilePos.y != y) {
					currentMouseTilePos.x = x;
					currentMouseTilePos.y = y;

					callbacks.mouseMoveChanged(currentMouseTilePos.x, currentMouseTilePos.y);
				}
				lastMouseEventCallback = new Date().getTime();
			}
		}

		function setBrowserSpecificPosFromMouseEventToItem(item,event) {
			if(event.movementX != undefined) { //chrome
				item.position.x += event.movementX;
				item.position.y += event.movementY;
			} else if(event.clientX != undefined) { //ff
				item.position.x = event.clientX;
				item.position.y = event.clientY;
			}
		}

		function getBrowserSpecificPosFromMouseEvent(event) {
			//TODO: does not work great in FF - finds pos with considerable offset
			var position = {};
			position.x = event.layerX;
			position.y = event.layerY;
			return position;
		}

		return mouseManager;
	}]);
})();