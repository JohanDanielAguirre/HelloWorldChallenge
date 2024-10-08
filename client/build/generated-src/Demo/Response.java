//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.10
//
// <auto-generated>
//
// Generated from file `Printer.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Demo;

public class Response extends com.zeroc.Ice.Value
{
    public Response()
    {
        this.value = "";
    }

    public Response(long responseTime, String value)
    {
        this.responseTime = responseTime;
        this.value = value;
    }

    public long responseTime;

    public String value;

    public Response clone()
    {
        return (Response)super.clone();
    }

    public static String ice_staticId()
    {
        return "::Demo::Response";
    }

    @Override
    public String ice_id()
    {
        return ice_staticId();
    }

    /** @hidden */
    public static final long serialVersionUID = 333378924L;

    /** @hidden */
    @Override
    protected void _iceWriteImpl(com.zeroc.Ice.OutputStream ostr_)
    {
        ostr_.startSlice(ice_staticId(), -1, true);
        ostr_.writeLong(responseTime);
        ostr_.writeString(value);
        ostr_.endSlice();
    }

    /** @hidden */
    @Override
    protected void _iceReadImpl(com.zeroc.Ice.InputStream istr_)
    {
        istr_.startSlice();
        responseTime = istr_.readLong();
        value = istr_.readString();
        istr_.endSlice();
    }
}
