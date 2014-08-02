/*
 * This file is part of Flow Commands, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commands.util;

import com.flowpowered.math.matrix.Matrix4f;
import com.flowpowered.math.vector.Vector3f;

public class RelativeVector3f extends Vector3f {
    private static final long serialVersionUID = -6825865575793208037L;
    private final boolean relativeX, relativeY, relativeZ;
    private final Vector3f refPoint, raw;

    public RelativeVector3f() {
        super();
        this.raw = Vector3f.ZERO;
        this.refPoint = Vector3f.ZERO;
        this.relativeX = this.relativeY = this.relativeZ = false;
    }

    public RelativeVector3f(Vector3f v, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(v);
        this.raw = v;
        this.refPoint = Vector3f.ZERO;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    public RelativeVector3f(double x, double y, double z, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this((float) x, (float) y, (float) z, relativeX, relativeY, relativeZ);
    }

    public RelativeVector3f(float x, float y, float z, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(x, y, z);
        this.raw = asVector();
        this.refPoint = Vector3f.ZERO;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    public RelativeVector3f(RelativeVector3f v, Vector3f refPoint) {
        super(resolve(refPoint, v.raw, v.relativeX, v.relativeY, v.relativeZ));
        this.relativeX = v.relativeX;
        this.relativeY = v.relativeY;
        this.relativeZ = v.relativeZ;
        this.refPoint = refPoint;
        this.raw = v.raw;
    }

    public RelativeVector3f(Vector3f v, Vector3f refPoint, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(resolve(refPoint, v, relativeX, relativeY, relativeZ));
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
        this.refPoint = refPoint;
        this.raw = v;
    }

    public RelativeVector3f withRefPoint(Vector3f refPoint) {
        if (refPoint == null) {
            refPoint = Vector3f.ZERO;
        }
        return new RelativeVector3f(this, refPoint);
    }

    public Vector3f getRaw() {
        return raw;
    }

    public Vector3f getRefPoint() {
        return refPoint;
    }

    public boolean isRelativeX() {
        return relativeX;
    }

    public boolean isRelativeY() {
        return relativeY;
    }

    public boolean isRelativeZ() {
        return relativeZ;
    }

    public Vector3f asVector() {
        return new Vector3f(this);
    }

    public Matrix4f asMatrix() {
        return new Matrix4f(
                relativeX ? 1 : 0, 0, 0, raw.getX(),
                0, relativeY ? 1 : 0, 0, raw.getY(),
                0, 0, relativeZ ? 1 : 0, raw.getZ(),
                0, 0, 0, 1);
    }

    protected static Vector3f resolve(Vector3f refPoint, Vector3f raw, boolean relativeX, boolean relativeY, boolean relativeZ) {
        double x = relativeX ? refPoint.getX() : 0;
        double y = relativeY ? refPoint.getY() : 0;
        double z = relativeZ ? refPoint.getZ() : 0;
        return raw.add(x, y, z);
    }
}
