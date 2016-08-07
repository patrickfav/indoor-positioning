(function() {
	'use strict';

	angular.module('signalmap-ng').factory('signalmapUpdateAndTransform',['signalmapState','signalmapUtils','signalmapCallbacks', function(state,utils,callbacks) {
		var updateService = {};

		updateService.syncPositionWithDataModel = function (mac) {
			if (state.getVertices()[mac]) {
				var node=null;

				if(state.getVertices()[mac].isManagedNode) {
					node = state.getSignalMapFromScope().managedNodes[mac];
				} else {
					node = state.getSignalMapFromScope().extendedNodes[mac];
				}

				if(node) {
					node.currentPos.x = Math.round(state.getVertices()[mac].path.position.x / state.getTileLenPx());
					node.currentPos.y = Math.round(state.getVertices()[mac].path.position.y / state.getTileLenPx());
				}
			}
		};

		updateService.updateEdge = function (mac) {
			if(state.getVertices()[mac]) {
				for(var key in state.getVertices()[mac].edges) {
					var edgeInfo = state.getVertices()[mac].edges[key];
					edgeInfo.edge.removeSegments();
					edgeInfo.edge.add(state.getVertices()[key].path.position);
					edgeInfo.edge.add(state.getVertices()[mac].path.position);
					edgeInfo.text.content = utils.toMeter(edgeInfo.edge.length);
					edgeInfo.text.position = utils.getVectorMidPoint(edgeInfo.edge,state.getLocalPaper());
					var rotate = utils.getTextAngle(edgeInfo.edge);
					edgeInfo.text.rotate(-edgeInfo.angle);
					edgeInfo.text.rotate(rotate);
					edgeInfo.angle=rotate;
				}
			}
		};

		updateService.syncAllNodesAndUpdate = function () {
			for(var mac in state.getVertices()) {
				updateService.syncPositionWithDataModel(mac);
			}
            updateService.updateView();
		};

		updateService.syncFloorplanAndUpdateView = function () {
			if(state.getFloorplanRaster()) {
				state.getFloorplanConfig().rotationDegrees = state.getFloorplanRaster().rotation;
				state.getFloorplanConfig().posX = state.getFloorplanRaster().position.x;
				state.getFloorplanConfig().posY = state.getFloorplanRaster().position.y;
				state.getFloorplanConfig().scale = state.getFloorplanRaster().scaling.x;
                updateService.updateView();
			}
		};

        updateService.flip = function(vertices,axisName,gridLengthAxisPx) {
            for(var mac in vertices) {
                var point = vertices[mac].path.position;
                if(point[axisName] > (gridLengthAxisPx / 2)) {
                    point[axisName] = (gridLengthAxisPx / 2) - (point[axisName] - (gridLengthAxisPx / 2));
                } else {
                    point[axisName] = (gridLengthAxisPx / 2) + ((gridLengthAxisPx / 2)-point[axisName] );
                }
                point = utils.getSnapPosition(point,state.getTileLenPx());
				updateService.updatePositionOfVertexGroup(mac,point);
                updateService.updateEdge(mac);
                updateService.syncPositionWithDataModel(mac);
            }
            updateService.syncAllNodesAndUpdate();
        };

		updateService.updatePositionOfVertexGroup = function(mac,newPosition) {
			if (state.getVertices()[mac]) {
				for(var i=0;i<state.getVertices()[mac].group.children.length;i++) {
					state.getVertices()[mac].group.children[i].position = newPosition;
				}
				state.getVertices()[mac].signalStrengthCircle.position = newPosition;
			}
		};

		updateService.syncRotation = function() {
			for(var mac in state.getVertices()) {
				updateService.syncVertexWithCoordinates(mac);
			}
			updateService.updateView();
		};

		updateService.syncVertexWithCoordinates = function (mac) {
			if (state.getVertices()[mac]) {
				state.getVertices()[mac].group.position = utils.getSnapPosition(state.getVertices()[mac].group.position,state.getTileLenPx());

				for(var i=0;i<state.getVertices()[mac].group.children.length;i++) {
					if(state.getVertices()[mac].group.children[i].rotation != 0) {
						state.getVertices()[mac].group.children[i].rotate(-state.getVertices()[mac].group.children[i].rotation);
					}
				}

				updateService.syncPositionWithDataModel(mac);
				updateService.updateEdge(mac);
			}
		};

        updateService.floorplanScale = function(scale,floorplanRaster,floorplanConfig) {
            if(scale && floorplanRaster) {
                floorplanConfig.scale = scale;
                var scaleOffset = 1/floorplanRaster.scaling.x;
                if(!scaleOffset || scaleOffset == 0) {
                    scaleOffset = 1;
                }
                floorplanRaster.scale(scale * scaleOffset);
                updateService.syncFloorplanAndUpdateView();
            }
        };

        updateService.setViewSize = function() {
            var usedGridLength = Math.max(state.getGridLengthX(), state.getGridLengthY());
            state.getLocalPaper().view.viewSize.height = usedGridLength * state.getTileLenPx();
            state.getLocalPaper().view.viewSize.width = usedGridLength * state.getTileLenPx();
            console.log("signalmap: set view size to "+state.getLocalPaper().view.viewSize.height+"x"+state.getLocalPaper().view.viewSize.width);
            updateService.updateView();
            return usedGridLength;
        };

        updateService.updateView = function () {
            state.getLocalPaper().view.update();
        };

        updateService.setCorrectZOrdering = function () {
			state.getGridGroup().sendToBack();
			state.getFloorplanRaster().sendToBack();
			state.getEdgeGroup().sendToBack();

			state.getLoadingLayerGroup().bringToFront();
			for(var mac in state.getVertices()) {
				if(state.getVertices()[mac].signalStrengthCircle) {
					state.getVertices()[mac].signalStrengthCircle.sendToBack();
				}
			}
		};

		updateService.switchShowExtendedNodes = function() {
			for(var mac in state.getVertices()) {
				if(state.getVertices()[mac].isManagedNode === false) {
					state.getVertices()[mac].group.visible = !state.getVertices()[mac].group.visible;
					for(var edgeMac in state.getVertices()[mac].edges) {
						state.getVertices()[mac].edges[edgeMac].edge.visible = !state.getVertices()[mac].edges[edgeMac].edge.visible;
						state.getVertices()[mac].edges[edgeMac].text.visible = !state.getVertices()[mac].edges[edgeMac].text.visible;
					}
				}
			}
			updateService.updateView();
		};

		updateService.switchShowSignalStrengthCircles = function() {
			for(var mac in state.getVertices()) {
				if(state.getVertices()[mac].signalStrengthCircle) {
					state.getVertices()[mac].signalStrengthCircle.visible = !state.getVertices()[mac].signalStrengthCircle.visible;
				}
			}
			updateService.updateView();
		};

		updateService.highlightTile = function(x,y,type) {
			var paper = state.getLocalPaper();
			if(paper) {
				var xPixel = x * state.getTileLenPx();
				var yPixel = y * state.getTileLenPx();

				var highlightTile = new paper.Path.Rectangle(xPixel,yPixel, state.getTileLenPx(),state.getTileLenPx());

				if(type) {
					if(type == 'good') {
						highlightTile.opacity = 0.3;
						highlightTile.fillColor = '#81CFE0';
					} else if(type == 'best') {
						highlightTile.opacity = 0.8;
						highlightTile.fillColor = '#90C695';
						state.setBestPosTile(highlightTile);
						utils.updateActualPosUI(state,callbacks);
					} else if(type == 'actual') {
						if(state.getActualPosTile() != undefined) {
							state.getActualPosTile().remove();
						}
						highlightTile.opacity = 0.8;
						highlightTile.fillColor = '#E74C3C';
						state.setActualPosTile(highlightTile);
						utils.updateActualPosUI(state,callbacks);
					}
				} else {
					highlightTile.fillColor = '#90C695';
				}

				highlightTile.bringToFront();
				updateService.updateView();
				console.log("signalmap: highlight ("+x+"|"+y+") "+type);
			}
		};

		return updateService;
	}]);
})();