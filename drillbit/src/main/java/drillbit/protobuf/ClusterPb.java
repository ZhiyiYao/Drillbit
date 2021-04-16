// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/protos/drillbit/protobuf/cluster_pb.proto

package drillbit.protobuf;

public final class ClusterPb {
  private ClusterPb() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface KMeansClassifierOrBuilder extends
      // @@protoc_insertion_point(interface_extends:drillbit.protobuf.KMeansClassifier)
      com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code drillbit.protobuf.KMeansClassifier}
   */
  public static final class KMeansClassifier extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:drillbit.protobuf.KMeansClassifier)
      KMeansClassifierOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use KMeansClassifier.newBuilder() to construct.
    private KMeansClassifier(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private KMeansClassifier() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new KMeansClassifier();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private KMeansClassifier(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_KMeansClassifier_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_KMeansClassifier_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              drillbit.protobuf.ClusterPb.KMeansClassifier.class, drillbit.protobuf.ClusterPb.KMeansClassifier.Builder.class);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof drillbit.protobuf.ClusterPb.KMeansClassifier)) {
        return super.equals(obj);
      }
      drillbit.protobuf.ClusterPb.KMeansClassifier other = (drillbit.protobuf.ClusterPb.KMeansClassifier) obj;

      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.KMeansClassifier parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(drillbit.protobuf.ClusterPb.KMeansClassifier prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code drillbit.protobuf.KMeansClassifier}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:drillbit.protobuf.KMeansClassifier)
        drillbit.protobuf.ClusterPb.KMeansClassifierOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_KMeansClassifier_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_KMeansClassifier_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                drillbit.protobuf.ClusterPb.KMeansClassifier.class, drillbit.protobuf.ClusterPb.KMeansClassifier.Builder.class);
      }

      // Construct using drillbit.protobuf.ClusterPb.KMeansClassifier.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_KMeansClassifier_descriptor;
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.KMeansClassifier getDefaultInstanceForType() {
        return drillbit.protobuf.ClusterPb.KMeansClassifier.getDefaultInstance();
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.KMeansClassifier build() {
        drillbit.protobuf.ClusterPb.KMeansClassifier result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.KMeansClassifier buildPartial() {
        drillbit.protobuf.ClusterPb.KMeansClassifier result = new drillbit.protobuf.ClusterPb.KMeansClassifier(this);
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof drillbit.protobuf.ClusterPb.KMeansClassifier) {
          return mergeFrom((drillbit.protobuf.ClusterPb.KMeansClassifier)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(drillbit.protobuf.ClusterPb.KMeansClassifier other) {
        if (other == drillbit.protobuf.ClusterPb.KMeansClassifier.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        drillbit.protobuf.ClusterPb.KMeansClassifier parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (drillbit.protobuf.ClusterPb.KMeansClassifier) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:drillbit.protobuf.KMeansClassifier)
    }

    // @@protoc_insertion_point(class_scope:drillbit.protobuf.KMeansClassifier)
    private static final drillbit.protobuf.ClusterPb.KMeansClassifier DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new drillbit.protobuf.ClusterPb.KMeansClassifier();
    }

    public static drillbit.protobuf.ClusterPb.KMeansClassifier getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<KMeansClassifier>
        PARSER = new com.google.protobuf.AbstractParser<KMeansClassifier>() {
      @java.lang.Override
      public KMeansClassifier parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new KMeansClassifier(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<KMeansClassifier> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<KMeansClassifier> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public drillbit.protobuf.ClusterPb.KMeansClassifier getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface GaussianMixtureModelClassifierOrBuilder extends
      // @@protoc_insertion_point(interface_extends:drillbit.protobuf.GaussianMixtureModelClassifier)
      com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code drillbit.protobuf.GaussianMixtureModelClassifier}
   */
  public static final class GaussianMixtureModelClassifier extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:drillbit.protobuf.GaussianMixtureModelClassifier)
      GaussianMixtureModelClassifierOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use GaussianMixtureModelClassifier.newBuilder() to construct.
    private GaussianMixtureModelClassifier(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private GaussianMixtureModelClassifier() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new GaussianMixtureModelClassifier();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private GaussianMixtureModelClassifier(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.class, drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.Builder.class);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier)) {
        return super.equals(obj);
      }
      drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier other = (drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier) obj;

      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code drillbit.protobuf.GaussianMixtureModelClassifier}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:drillbit.protobuf.GaussianMixtureModelClassifier)
        drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifierOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.class, drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.Builder.class);
      }

      // Construct using drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return drillbit.protobuf.ClusterPb.internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor;
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier getDefaultInstanceForType() {
        return drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.getDefaultInstance();
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier build() {
        drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier buildPartial() {
        drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier result = new drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier(this);
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier) {
          return mergeFrom((drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier other) {
        if (other == drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:drillbit.protobuf.GaussianMixtureModelClassifier)
    }

    // @@protoc_insertion_point(class_scope:drillbit.protobuf.GaussianMixtureModelClassifier)
    private static final drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier();
    }

    public static drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<GaussianMixtureModelClassifier>
        PARSER = new com.google.protobuf.AbstractParser<GaussianMixtureModelClassifier>() {
      @java.lang.Override
      public GaussianMixtureModelClassifier parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new GaussianMixtureModelClassifier(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<GaussianMixtureModelClassifier> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<GaussianMixtureModelClassifier> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public drillbit.protobuf.ClusterPb.GaussianMixtureModelClassifier getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_drillbit_protobuf_KMeansClassifier_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_drillbit_protobuf_KMeansClassifier_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n3resources/protos/drillbit/protobuf/clu" +
      "ster_pb.proto\022\021drillbit.protobuf\"\022\n\020KMea" +
      "nsClassifier\" \n\036GaussianMixtureModelClas" +
      "sifierb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_drillbit_protobuf_KMeansClassifier_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_drillbit_protobuf_KMeansClassifier_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_drillbit_protobuf_KMeansClassifier_descriptor,
        new java.lang.String[] { });
    internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_drillbit_protobuf_GaussianMixtureModelClassifier_descriptor,
        new java.lang.String[] { });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
