precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;
uniform sampler2D vTextureSrc;
void main() {
    vec4 vColor = texture2D(vTextureSrc, textureCoordinate);
    if (vColor.a == 0.0){
         gl_FragColor=vec4(0.0,0.0,0.0,0.0);
    }else{
        gl_FragColor= texture2D(vTexture, textureCoordinate);
    }

}