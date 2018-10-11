package net.lukecameron.marvelcharacterviewer;

public class TestMath {

    float a;
    float b;
    float c;

    float answer;

    public void SetValues(float a, float b, float c){
        this.a = a;
        this.b = b;
        this.c = c;
    }


    public float MultiplyAll(){
        answer = a * b * c;
        return answer;
    }
}
