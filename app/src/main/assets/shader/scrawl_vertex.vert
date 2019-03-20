attribute vec4 vPosition;
attribute vec2 vCoord;
attribute vec2 vCoord2;
uniform mat4 vMatrix;

varying vec2 textureCoordinate;
varying vec2 textureCoordinate2;

void main(){
    gl_Position = vMatrix*vPosition;
    textureCoordinate = vCoord;
    textureCoordinate2 = vCoord2;
}