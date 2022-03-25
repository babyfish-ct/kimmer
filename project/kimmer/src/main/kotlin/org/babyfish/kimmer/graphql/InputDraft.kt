package org.babyfish.kimmer.graphql

import org.babyfish.kimmer.Draft

interface InputDraft<T: Input>: Input, Draft<T>